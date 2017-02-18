package inc.itnity.elbilad.utils.rx_downloader;

/**
 * Created by st1ch on 18.02.17.
 */

public class LongSparseArray<E> implements Cloneable {
  private static final Object DELETED = new Object();
  private boolean mGarbage = false;

  private long[] mKeys;
  private Object[] mValues;
  private int mSize;

  /**
   * Creates a new LongSparseArray containing no mappings.
   */
  public LongSparseArray() {
    this(10);
  }

  /**
   * Creates a new LongSparseArray containing no mappings that will not
   * require any additional memory allocation to store the specified
   * number of mappings.  If you supply an initial capacity of 0, the
   * sparse array will be initialized with a light-weight representation
   * not requiring any additional array allocations.
   */
  public LongSparseArray(int initialCapacity) {
    if (initialCapacity == 0) {
      mKeys = ContainerHelpers.EMPTY_LONGS;
      mValues = ContainerHelpers.EMPTY_OBJECTS;
    } else {
      initialCapacity = ContainerHelpers.idealLongArraySize(initialCapacity);
      mKeys = new long[initialCapacity];
      mValues = new Object[initialCapacity];
    }
    mSize = 0;
  }

  @Override @SuppressWarnings("unchecked") public LongSparseArray<E> clone() {
    LongSparseArray<E> clone = null;
    try {
      clone = (LongSparseArray<E>) super.clone();
      clone.mKeys = mKeys.clone();
      clone.mValues = mValues.clone();
    } catch (CloneNotSupportedException cnse) {
            /* ignore */
    }
    return clone;
  }

  /**
   * Gets the Object mapped from the specified key, or <code>null</code>
   * if no such mapping has been made.
   */
  public E get(long key) {
    return get(key, null);
  }

  /**
   * Gets the Object mapped from the specified key, or the specified Object
   * if no such mapping has been made.
   */
  @SuppressWarnings("unchecked") public E get(long key, E valueIfKeyNotFound) {
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);

    if (i < 0 || mValues[i] == DELETED) {
      return valueIfKeyNotFound;
    } else {
      return (E) mValues[i];
    }
  }

  /**
   * Removes the mapping from the specified key, if there was any.
   */
  public void delete(long key) {
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);

    if (i >= 0) {
      if (mValues[i] != DELETED) {
        mValues[i] = DELETED;
        mGarbage = true;
      }
    }
  }

  /**
   * Alias for {@link #delete(long)}.
   */
  public void remove(long key) {
    delete(key);
  }

  /**
   * Removes the mapping at the specified index.
   */
  public void removeAt(int index) {
    if (mValues[index] != DELETED) {
      mValues[index] = DELETED;
      mGarbage = true;
    }
  }

  private void gc() {
    // Log.e("SparseArray", "gc start with " + mSize);

    int n = mSize;
    int o = 0;
    long[] keys = mKeys;
    Object[] values = mValues;

    for (int i = 0; i < n; i++) {
      Object val = values[i];

      if (val != DELETED) {
        if (i != o) {
          keys[o] = keys[i];
          values[o] = val;
          values[i] = null;
        }

        o++;
      }
    }

    mGarbage = false;
    mSize = o;

    // Log.e("SparseArray", "gc end with " + mSize);
  }

  /**
   * Adds a mapping from the specified key to the specified value,
   * replacing the previous mapping from the specified key if there
   * was one.
   */
  public void put(long key, E value) {
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);

    if (i >= 0) {
      mValues[i] = value;
    } else {
      i = ~i;

      if (i < mSize && mValues[i] == DELETED) {
        mKeys[i] = key;
        mValues[i] = value;
        return;
      }

      if (mGarbage && mSize >= mKeys.length) {
        gc();

        // Search again because indices may have changed.
        i = ~ContainerHelpers.binarySearch(mKeys, mSize, key);
      }

      if (mSize >= mKeys.length) {
        int n = ContainerHelpers.idealLongArraySize(mSize + 1);

        long[] nkeys = new long[n];
        Object[] nvalues = new Object[n];

        // Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
        System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
        System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

        mKeys = nkeys;
        mValues = nvalues;
      }

      if (mSize - i != 0) {
        // Log.e("SparseArray", "move " + (mSize - i));
        System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
        System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
      }

      mKeys[i] = key;
      mValues[i] = value;
      mSize++;
    }
  }

  /**
   * Returns the number of key-value mappings that this LongSparseArray
   * currently stores.
   */
  public int size() {
    if (mGarbage) {
      gc();
    }

    return mSize;
  }

  /**
   * Given an index in the range <code>0...size()-1</code>, returns
   * the key from the <code>index</code>th key-value mapping that this
   * LongSparseArray stores.
   */
  public long keyAt(int index) {
    if (mGarbage) {
      gc();
    }

    return mKeys[index];
  }

  /**
   * Given an index in the range <code>0...size()-1</code>, returns
   * the value from the <code>index</code>th key-value mapping that this
   * LongSparseArray stores.
   */
  @SuppressWarnings("unchecked") public E valueAt(int index) {
    if (mGarbage) {
      gc();
    }

    return (E) mValues[index];
  }

  /**
   * Given an index in the range <code>0...size()-1</code>, sets a new
   * value for the <code>index</code>th key-value mapping that this
   * LongSparseArray stores.
   */
  public void setValueAt(int index, E value) {
    if (mGarbage) {
      gc();
    }

    mValues[index] = value;
  }

  /**
   * Returns the index for which {@link #keyAt} would return the
   * specified key, or a negative number if the specified
   * key is not mapped.
   */
  public int indexOfKey(long key) {
    if (mGarbage) {
      gc();
    }

    return ContainerHelpers.binarySearch(mKeys, mSize, key);
  }

  /**
   * Returns an index for which {@link #valueAt} would return the
   * specified key, or a negative number if no keys map to the
   * specified value.
   * Beware that this is a linear search, unlike lookups by key,
   * and that multiple keys can map to the same value and this will
   * find only one of them.
   */
  public int indexOfValue(E value) {
    if (mGarbage) {
      gc();
    }

    for (int i = 0; i < mSize; i++)
      if (mValues[i] == value) return i;

    return -1;
  }

  /**
   * Removes all key-value mappings from this LongSparseArray.
   */
  public void clear() {
    int n = mSize;
    Object[] values = mValues;

    for (int i = 0; i < n; i++) {
      values[i] = null;
    }

    mSize = 0;
    mGarbage = false;
  }

  /**
   * Puts a key/value pair into the array, optimizing for the case where
   * the key is greater than all existing keys in the array.
   */
  public void append(long key, E value) {
    if (mSize != 0 && key <= mKeys[mSize - 1]) {
      put(key, value);
      return;
    }

    if (mGarbage && mSize >= mKeys.length) {
      gc();
    }

    int pos = mSize;
    if (pos >= mKeys.length) {
      int n = ContainerHelpers.idealLongArraySize(pos + 1);

      long[] nkeys = new long[n];
      Object[] nvalues = new Object[n];

      // Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
      System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
      System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

      mKeys = nkeys;
      mValues = nvalues;
    }

    mKeys[pos] = key;
    mValues[pos] = value;
    mSize = pos + 1;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation composes a string by iterating over its mappings. If
   * this map contains itself as a value, the string "(this Map)"
   * will appear in its place.
   */
  @Override public String toString() {
    if (size() <= 0) {
      return "{}";
    }

    StringBuilder buffer = new StringBuilder(mSize * 28);
    buffer.append('{');
    for (int i = 0; i < mSize; i++) {
      if (i > 0) {
        buffer.append(", ");
      }
      long key = keyAt(i);
      buffer.append(key);
      buffer.append('=');
      Object value = valueAt(i);
      if (value != this) {
        buffer.append(value);
      } else {
        buffer.append("(this Map)");
      }
    }
    buffer.append('}');
    return buffer.toString();
  }
}
