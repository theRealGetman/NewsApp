package inc.itnity.elbilad.data.repositories;

import inc.itnity.elbilad.data.repositories.remote.ElbiladRemoteDataSource;
import inc.itnity.elbilad.domain.models.Journal;
import inc.itnity.elbilad.domain.models.article.Article;
import inc.itnity.elbilad.domain.models.article.Bookmark;
import inc.itnity.elbilad.domain.models.article.HomeArticles;
import inc.itnity.elbilad.domain.models.article.Image;
import inc.itnity.elbilad.domain.models.article.Video;
import inc.itnity.elbilad.domain.models.categorie.Category;
import io.reactivecache.Provider;
import io.reactivecache.ProviderGroup;
import io.reactivecache.ReactiveCache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;

/**
 * Created by st1ch on 15.01.17.
 */

public class ElbiladRepositoryImpl implements ElbiladRepository {

  private ElbiladRemoteDataSource remoteDataSource;
  private final Provider<List<Category>> categoryListCache;
  private final Provider<List<Article>> articleListCache;
  private final Provider<List<Article>> lastNews6Cache;
  private final Provider<List<Article>> mostReadCache;
  private final Provider<List<Article>> lastNewsCache;
  private final Provider<HomeArticles> homeArticlesCache;
  private final ProviderGroup<List<Article>> categoryArticleListCache;
  private final ProviderGroup<Article> articleCache;
  private final Provider<List<Bookmark>> bookmarkedArticlesCache;
  private final Provider<List<Video>> videosCache;
  private final Provider<List<Image>> photosCache;
  private final Provider<Journal> journalCache;

  public ElbiladRepositoryImpl(ElbiladRemoteDataSource remoteDataSource,
      ReactiveCache reactiveCache) {
    this.remoteDataSource = remoteDataSource;
    this.categoryListCache = reactiveCache.<List<Category>>provider().withKey("categoryListCache");
    this.articleListCache = reactiveCache.<List<Article>>provider().withKey("articleListCache");
    this.homeArticlesCache = reactiveCache.<HomeArticles>provider().withKey("homeArticlesCache");
    this.categoryArticleListCache =
        reactiveCache.<List<Article>>providerGroup().lifeCache(1, TimeUnit.DAYS)
            .withKey("categoryArticleListCache");
    this.articleCache =
        reactiveCache.<Article>providerGroup().lifeCache(1, TimeUnit.DAYS).withKey("articleCache");
    this.lastNews6Cache = reactiveCache.<List<Article>>provider().withKey("lastNews6Cache");
    this.lastNewsCache = reactiveCache.<List<Article>>provider().withKey("lastNewsCache");
    this.mostReadCache = reactiveCache.<List<Article>>provider().withKey("mostReadCache");
    this.bookmarkedArticlesCache = reactiveCache.<List<Bookmark>>provider().withKey("bookmarks");
    this.videosCache = reactiveCache.<List<Video>>provider().withKey("videosCache");
    this.photosCache = reactiveCache.<List<Image>>provider().withKey("photosCache");
    this.journalCache = reactiveCache.<Journal>provider().withKey("journalCache");
  }

  @Override public Observable<Boolean> loadCategoriesAndHomeArticles(boolean refresh) {
    if (refresh) {
      return Observable.zip(remoteDataSource.getCategories().compose(categoryListCache.replace()),
          remoteDataSource.getHomeArticles().compose(homeArticlesCache.replace()),
          (categories, articles) -> true);
    }
    return Observable.zip(
        remoteDataSource.getCategories().compose(categoryListCache.readWithLoader()),
        remoteDataSource.getHomeArticles().compose(homeArticlesCache.readWithLoader()),
        (categories, articles) -> true);
  }

  @Override public Observable<List<Category>> getCategories(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getCategories().compose(categoryListCache.replace());
    }
    return remoteDataSource.getCategories().compose(categoryListCache.readWithLoader());
  }

  @Override public Observable<HomeArticles> getHomeArticles(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getHomeArticles().compose(homeArticlesCache.replace());
    }
    return remoteDataSource.getHomeArticles().compose(homeArticlesCache.readWithLoader());
  }

  @Override public Observable<List<Article>> getArticles(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getArticles().compose(articleListCache.replace());
    }
    return remoteDataSource.getArticles().compose(articleListCache.readWithLoader());
  }

  @Override public Observable<List<Article>> getMostReadArticles(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getMostReadArticles().compose(mostReadCache.replace());
    }
    return remoteDataSource.getMostReadArticles().compose(mostReadCache.readWithLoader());
  }

  @Override public Observable<List<Article>> getCategoryArticles(boolean refresh, int categoryId) {
    if (refresh) {
      return remoteDataSource.getCategoryArticles(categoryId)
          .compose(categoryArticleListCache.replace(categoryId));
    }
    return remoteDataSource.getCategoryArticles(categoryId)
        .compose(categoryArticleListCache.readWithLoader(categoryId));
  }

  @Override public Observable<Article> getArticle(boolean refresh, String articleId) {
    if (refresh) {
      return remoteDataSource.getArticle(articleId)
          .compose(articleCache.replace(articleId))
          .compose(checkArticleBookmarked(articleId));
    }
    return remoteDataSource.getArticle(articleId)
        .compose(articleCache.readWithLoader(articleId))
        .compose(checkArticleBookmarked(articleId));
  }

  @Override public Observable<List<Video>> getVideoList(boolean refresh) {
    if (refresh) {
      return getHomeArticles(true).map(HomeArticles::getVideos);
    }
    return getHomeArticles(false).map(HomeArticles::getVideos);
  }

  @Override public Observable<List<Article>> getLastNews(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getLastNews().compose(lastNewsCache.replace());
    }
    return remoteDataSource.getLastNews().compose(lastNewsCache.readWithLoader());
  }

  @Override public Observable<List<Article>> getLast6News(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getLast6News().compose(lastNews6Cache.replace());
    }
    return remoteDataSource.getLast6News().compose(lastNews6Cache.readWithLoader());
  }

  @Override public Observable<List<Video>> getVideos(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getVideos().compose(videosCache.replace());
    }
    return remoteDataSource.getVideos().compose(videosCache.readWithLoader());
  }

  @Override public Observable<Video> getVideo(String videoId) {
    return videosCache.read()
        .flatMap(Observable::from)
        .filter(video -> video.getId().equals(videoId))
        .compose(checkVideoBookmarked(videoId));
  }

  @Override public Observable<Video> getVideoArticle(String videoId) {
    return homeArticlesCache.read()
        .map(HomeArticles::getVideoArticles)
        .flatMap(Observable::from)
        .filter(articleVideo -> articleVideo.getId().equals(videoId))
        .map(Video::new);
  }

  @Override public Observable<List<Image>> getGallery(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getGallery().compose(photosCache.replace());
    }
    return remoteDataSource.getGallery().compose(photosCache.readWithLoader());
  }

  @Override public Observable<Bookmark> addToBookmark(Bookmark bookmark) {
    return bookmarkedArticlesCache.readNullable().map(articles -> {
      if (articles == null) {
        articles = new ArrayList<>();
      }
      return articles;
    }).map(articles -> {
      articles.add(0, bookmark);
      return articles;
    }).compose(bookmarkedArticlesCache.replace()).map(articles -> bookmark);
  }

  @Override public Observable<Bookmark> removeBookmark(Bookmark bookmark) {
    return bookmarkedArticlesCache.read().map(articles -> {
      articles.remove(bookmark);
      return articles;
    }).compose(bookmarkedArticlesCache.replace()).map(articles -> bookmark);
  }

  @Override public Observable<List<Bookmark>> getBookmarks() {
    return bookmarkedArticlesCache.readNullable();
  }

  @Override public Observable<Journal> getJournalData(boolean refresh) {
    if (refresh) {
      return remoteDataSource.getJournalData().compose(journalCache.replace());
    }
    return remoteDataSource.getJournalData().compose(journalCache.readWithLoader());
  }

  @Override public Observable<String> downloadJournal(String url, String filename) {
    return remoteDataSource.downloadJournal(url, filename);
  }

  private Observable<Boolean> checkArticleIsBookmarked(String articleId) {
    return getBookmarks().flatMap(bookmarks -> {
      boolean bookmarked = false;
      if (bookmarks == null) {
        return Observable.just(bookmarked);
      }
      for (Bookmark bookmark : bookmarks) {
        if (bookmark.getArticle() != null && bookmark.getArticle().getId().equals(articleId)) {
          bookmarked = true;
        }
      }
      return Observable.just(bookmarked);
    });
  }

  private Observable<Boolean> checkVideoIsBookmarked(String articleId) {
    return getBookmarks().flatMap(bookmarks -> {
      boolean bookmarked = false;
      if (bookmarks == null) {
        return Observable.just(bookmarked);
      }
      for (Bookmark bookmark : bookmarks) {
        if (bookmark.getVideo() != null && bookmark.getVideo().getId().equals(articleId)) {
          bookmarked = true;
        }
      }
      return Observable.just(bookmarked);
    });
  }

  //private Observable<Boolean> checkPhotoIsBookmarked(String articleId) {
  //  return getBookmarks().flatMap(bookmarks -> {
  //    boolean bookmarked = false;
  //    if (bookmarks == null) {
  //      return Observable.just(bookmarked);
  //    }
  //    for (Bookmark bookmark : bookmarks) {
  //      if (bookmark.getPhoto() != null && bookmark.getPhoto().getId().equals(articleId)) {
  //        bookmarked = true;
  //      }
  //    }
  //    return Observable.just(bookmarked);
  //  });
  //}

  private <T> Observable.Transformer<Article, Article> checkArticleBookmarked(String articleId) {
    return observable -> Observable.zip(observable, checkArticleIsBookmarked(articleId),
        (article, isBookmarked) -> {
          article.setBookmarked(isBookmarked);
          return article;
        });
  }

  private <T> Observable.Transformer<Video, Video> checkVideoBookmarked(String videoId) {
    return observable -> Observable.zip(observable, checkVideoIsBookmarked(videoId),
        (video, isBookmarked) -> {
          video.setBookmarked(isBookmarked);
          return video;
        });
  }
}
