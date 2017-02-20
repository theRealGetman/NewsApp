package inc.itnity.elbilad.presentation.views;

import inc.itnity.elbilad.domain.models.article.Image;
import inc.itnity.elbilad.presentation.views.base.ConnectionView;
import java.util.List;

/**
 * Created by st1ch on 15.02.17.
 */

public interface PhotoDetailsView extends ConnectionView {
  void showSlideshow(List<Image> photos);

  void showAddedToBookmarks();
}