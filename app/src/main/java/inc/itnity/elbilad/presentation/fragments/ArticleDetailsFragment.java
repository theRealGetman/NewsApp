package inc.itnity.elbilad.presentation.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import inc.itnity.elbilad.R;
import inc.itnity.elbilad.constants.ApiConfig;
import inc.itnity.elbilad.domain.models.article.Article;
import inc.itnity.elbilad.presentation.activities.MainActivity;
import inc.itnity.elbilad.presentation.activities.base.AbstractBaseActivity;
import inc.itnity.elbilad.presentation.fragments.base.AbstractBaseFragment;
import inc.itnity.elbilad.presentation.presenters.ArticleDetailsPresenter;
import inc.itnity.elbilad.presentation.views.ArticleDetailsView;
import inc.itnity.elbilad.utils.ElbiladUtils;
import inc.itnity.elbilad.utils.ImageLoaderHelper;
import javax.inject.Inject;

/**
 * Created by st1ch on 05.02.17.
 */

public class ArticleDetailsFragment extends AbstractBaseFragment implements ArticleDetailsView {

  private static final String ARG_ARTICLE_ID = "article_id_arg";

  public static ArticleDetailsFragment newInstance(String articleId) {
    Bundle args = new Bundle();
    args.putString(ARG_ARTICLE_ID, articleId);
    ArticleDetailsFragment fragment = new ArticleDetailsFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @BindView(R.id.tv_text) TextView tvText;
  @BindView(R.id.tv_title) TextView tvTitle;
  @BindView(R.id.tv_category) TextView tvCategory;
  @BindView(R.id.tv_date) TextView tvDate;
  @BindView(R.id.iv_image) ImageView ivImage;
  @BindView(R.id.tv_title_text) TextView tvTitleText;

  @Inject ImageLoaderHelper imageLoaderHelper;

  @Inject ElbiladUtils elbiladUtils;

  @Inject ArticleDetailsPresenter presenter;

  @Override public int getContentView() {
    return R.layout.fragment_article_details;
  }

  @Override public void injectComponent() {
    MainActivity.getMainActivityComponent().inject(this);
  }

  @Override protected void bindPresenter() {
    presenter.bind(this);
  }

  @Override protected void unbindPresenter() {
    presenter.onDestroy();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = super.onCreateView(inflater, container, savedInstanceState);

    presenter.onCreate(getArguments().getString(ARG_ARTICLE_ID));

    return rootView;
  }

  @OnClick(R.id.iv_share) protected void onShareClick() {
  }

  @Override public void showArticle(Article article) {
    ((AbstractBaseActivity) getActivity()).showDetailToolbar(article.getTitle());

    tvText.setText(Html.fromHtml(article.getText()));
    tvTitle.setText(article.getTitle());
    tvTitleText.setText(article.getPreview());
    tvCategory.setText(article.getAuthor());
    tvDate.setText(elbiladUtils.getArticleTimeDate(article.getTime(), article.getDate()));

    if (!TextUtils.isEmpty(article.getImage())) {
      imageLoaderHelper.loadUrlImage(
          ApiConfig.IMAGE_BASE_URL + ApiConfig.LARGE + article.getImage(), ivImage);
    }
  }
}
