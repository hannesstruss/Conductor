package de.hannesstruss.nullparenttest;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;

public class ParentController extends Controller {
  ViewGroup root;

  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    root = new FrameLayout(inflater.getContext());
    return root;
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
                               @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);

    if (changeType == ControllerChangeType.PUSH_ENTER && !getChildRouter(root).hasRootController()) {
      getChildRouter(root).pushController(RouterTransaction.with(new MidLevelController()));
    }
  }
}
