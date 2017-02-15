package inc.itnity.elbilad.domain.models.article;

import com.google.gson.annotations.SerializedName;

/**
 * Created by st1ch on 15.01.17.
 */

public class Video extends BaseArticle {

  @SerializedName("id_video_categorie") private int categoryId;
  @SerializedName("description") private String preview;
  @SerializedName("une") private String une;
  @SerializedName("type_video") private String videoType;
  @SerializedName("youtube_id") private String youtubeId;
  @SerializedName("local_video") private String localVideo;

  public Video() {
    super.setType(TYPE.VIDEO);
  }

  @Override public String getPreview() {
    return preview;
  }

  public int getCategoryId() {
    return categoryId;
  }

  public String getUne() {
    return une;
  }

  public String getVideoType() {
    return videoType;
  }

  public String getYoutubeId() {
    return youtubeId;
  }

  public String getLocalVideo() {
    return localVideo;
  }

  @Override public String toString() {
    return super.toString()
        + " Video{"
        + "categoryId="
        + categoryId
        + ", preview='"
        + preview
        + '\''
        + ", une='"
        + une
        + '\''
        + ", videoType='"
        + videoType
        + '\''
        + ", youtubeId='"
        + youtubeId
        + '\''
        + ", localVideo='"
        + localVideo
        + '\''
        + '}';
  }
}
