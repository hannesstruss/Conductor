package thegrid.io.viewpagerinitdemo;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;

public class MiddleController extends Controller {
  private FrameLayout container;

  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    FrameLayout frameLayout = new FrameLayout(inflater.getContext());

    frameLayout.setBackgroundColor(0xFFCCFFCC);
    frameLayout.setPadding(50, 50, 50, 50);
    this.container = frameLayout;
    return frameLayout;

  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
                               @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    if (changeType == ControllerChangeType.PUSH_ENTER) {
      getChildRouter(container, null).pushController(RouterTransaction.with(new TabController(0)));
    }
  }
}
