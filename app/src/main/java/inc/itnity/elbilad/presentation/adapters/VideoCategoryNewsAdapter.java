package inc.itnity.elbilad.presentation.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import inc.itnity.elbilad.R;
import inc.itnity.elbilad.constants.ApiConfig;
import inc.itnity.elbilad.domain.models.article.ArticleItem;
import inc.itnity.elbilad.domain.models.article.Video;
import inc.itnity.elbilad.utils.ElbiladUtils;
import inc.itnity.elbilad.utils.ImageLoaderHelper;
import inc.itnity.elbilad.utils.YouTubeHelper;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by st1ch on 04.02.17.
 */

public class VideoCategoryNewsAdapter
    extends RecyclerView.Adapter<VideoCategoryNewsAdapter.SimpleNewsViewHolder> {

  private static final int TYPE_TOP = 0;
  private static final int TYPE_SIMPLE = 1;
  private static final int TYPE_BANNER_100 = 2;
  private static final int TYPE_BANNER_50 = 3;

  private List<Video> articles = new ArrayList<>();

  private ImageLoaderHelper imageLoaderHelper;
  private ElbiladUtils elbiladUtils;
  private FragmentManager childFragmentManager;
  //private FragmentNavigator fragmentNavigator;
  private YouTubeHelper youTubeHelper;
  private RecyclerView recyclerView;
  private boolean needAutoStart = false;
  private String currentItemId;
  private int currentItemPosition;

  @Inject VideoCategoryNewsAdapter(ImageLoaderHelper imageLoaderHelper, ElbiladUtils elbiladUtils,
      //FragmentNavigator fragmentNavigator
      YouTubeHelper youTubeHelper) {
    this.imageLoaderHelper = imageLoaderHelper;
    this.elbiladUtils = elbiladUtils;
    //this.fragmentNavigator = fragmentNavigator;
    this.youTubeHelper = youTubeHelper;
  }

  @Override public int getItemViewType(int position) {
    if (position == 0) {
      return TYPE_TOP;
    }

    if (getItem(position).getType() == ArticleItem.TYPE.BANNER_100) {
      return TYPE_BANNER_100;
    } else if (getItem(position).getType() == ArticleItem.TYPE.BANNER_50) {
      return TYPE_BANNER_50;
    }

    return TYPE_SIMPLE;
  }

  @Override public SimpleNewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == TYPE_TOP) {
      return new TopNewsViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_video_news_top, parent, false));
    }

    if (viewType == TYPE_BANNER_100) {
      return new BannerViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_banner_4588, parent, false));
    }

    if (viewType == TYPE_BANNER_50) {
      return new BannerViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_banner_6582, parent, false));
    }

    return new SimpleNewsViewHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_news, parent, false));
  }

  @Override public void onBindViewHolder(SimpleNewsViewHolder holder, int position) {
    int viewType = getItemViewType(position);

    if (viewType != TYPE_BANNER_100 && viewType != TYPE_BANNER_50) {
      Video article = getItem(position);

      if (viewType == TYPE_TOP) {
        ((TopNewsViewHolder) holder).tvPreview.setText(article.getPreview());
        String urlVideo = article.getYoutubeId();

        if (youTubeHelper.isYoutubeInstalled()) {
          holder.ivAvatar.setVisibility(View.INVISIBLE);
          ((TopNewsViewHolder) holder).youtubeView.setVisibility(View.VISIBLE);
          YouTubePlayerSupportFragment youTubePlayerFragment =
              YouTubePlayerSupportFragment.newInstance();

          childFragmentManager.beginTransaction()
              .replace(R.id.youtube_view, youTubePlayerFragment)
              .commit();

          youTubePlayerFragment.initialize(ApiConfig.YOUTUBE_KEY,
              new YouTubePlayer.OnInitializedListener() {
                @Override public void onInitializationSuccess(YouTubePlayer.Provider provider,
                    YouTubePlayer youTubePlayer, boolean wasRestored) {
                  youTubePlayer.setFullscreen(false);
                  if (!wasRestored && !urlVideo.isEmpty()) {
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    youTubePlayer.setFullscreenControlFlags(
                        YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                    if (needAutoStart) {
                      youTubePlayer.loadVideo(urlVideo);
                    } else {
                      youTubePlayer.cueVideo(urlVideo);
                    }
                    youTubePlayer.setOnFullscreenListener(b -> {
                      youTubeHelper.startPlayer(urlVideo);
                    });
                  }
                }

                @Override public void onInitializationFailure(YouTubePlayer.Provider provider,
                    YouTubeInitializationResult youTubeInitializationResult) {
                }
              });
        } else {
          ((TopNewsViewHolder) holder).youtubeView.setVisibility(View.INVISIBLE);
          holder.ivAvatar.setVisibility(View.VISIBLE);

          if (!TextUtils.isEmpty(article.getImage())) {
            imageLoaderHelper.loadVideoImageLarge(article.getImage(), holder.ivAvatar);
          }

          holder.itemView.setOnClickListener(
              v -> youTubeHelper.startPlayer(article.getYoutubeId()));
        }
      } else {
        if (!TextUtils.isEmpty(article.getImage())) {
          imageLoaderHelper.loadVideoImageThumb(article.getImage(), holder.ivAvatar);
        }

        holder.itemView.setOnClickListener(v -> moveToTop(position, article));
      }

      holder.tvCategory.setText(article.getCategoryTitle());
      holder.tvTitle.setText(article.getTitle());
      holder.tvDate.setText(elbiladUtils.getArticleTimeDate(article.getTime(), article.getDate()));

      //holder.itemView.setOnClickListener(
      //    v -> fragmentNavigator.startVideoDetailsFragment(article.getId(), false));
    }
  }

  @Override public int getItemCount() {
    return articles.size();
  }

  private Video getItem(int position) {
    return articles.get(position);
  }

  public void setArticles(List<Video> articles) {
    this.articles.clear();

    if (articles.size() > 8) {
      for (int i = 0; i < 3; i++) {
        this.articles.add(articles.get(i));
      }
      this.articles.add(new Video(ArticleItem.TYPE.BANNER_100));
      for (int i = 3; i < 8; i++) {
        this.articles.add(articles.get(i));
      }
      this.articles.add(new Video(ArticleItem.TYPE.BANNER_50));
      for (int i = 8; i < articles.size(); i++) {
        this.articles.add(articles.get(i));
      }
    } else if (articles.size() > 3) {
      for (int i = 0; i < 3; i++) {
        this.articles.add(articles.get(i));
      }
      this.articles.add(new Video(ArticleItem.TYPE.BANNER_100));
      for (int i = 3; i < articles.size(); i++) {
        this.articles.add(articles.get(i));
      }
    } else {
      this.articles.addAll(articles);
    }

    notifyDataSetChanged();
  }

  private void moveToTop(int position, Video video) {
    currentItemPosition = position;
    this.articles.remove(position);
    this.articles.add(0, video);
    notifyDataSetChanged();
    if (recyclerView != null) {
      recyclerView.scrollToPosition(0);
    }
  }

  public void selectCurrentItem() {
    if (!TextUtils.isEmpty(currentItemId) && hasVideo(currentItemId)) {
      Video item = getItem(currentItemPosition);
      this.articles.remove(currentItemPosition);
      this.articles.add(0, item);
      notifyDataSetChanged();
      if (recyclerView != null) {
        recyclerView.scrollToPosition(0);
      }
    }
  }

  private boolean hasVideo(String itemId) {
    for (int i = 0; i < articles.size(); i++) {
      Video video = articles.get(i);
      String videoId = video.getId();
      if (!TextUtils.isEmpty(videoId) && videoId.equals(itemId)) {
        currentItemPosition = i;
        return true;
      }
    }
    return false;
  }

  public void setChildFragmentManager(FragmentManager childFragmentManager) {
    this.childFragmentManager = childFragmentManager;
  }

  public void setCurrentItemId(String currentItemId) {
    this.currentItemId = currentItemId;
  }

  public void setRecyclerView(RecyclerView recyclerView) {
    this.recyclerView = recyclerView;
  }

  class SimpleNewsViewHolder extends RecyclerView.ViewHolder {

    @Nullable @BindView(R.id.iv_image) ImageView ivAvatar;
    @Nullable @BindView(R.id.tv_date) TextView tvDate;
    @Nullable @BindView(R.id.tv_category) TextView tvCategory;
    @Nullable @BindView(R.id.tv_title) TextView tvTitle;

    SimpleNewsViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  class TopNewsViewHolder extends SimpleNewsViewHolder {

    @BindView(R.id.tv_preview) TextView tvPreview;
    @BindView(R.id.youtube_view) View youtubeView;

    TopNewsViewHolder(View itemView) {
      super(itemView);
    }
  }

  class BannerViewHolder extends SimpleNewsViewHolder {

    @BindView(R.id.adView) AdView adView;

    BannerViewHolder(View itemView) {
      super(itemView);
      AdRequest adRequest = new AdRequest.Builder().build();
      adView.loadAd(adRequest);
    }
  }
}
