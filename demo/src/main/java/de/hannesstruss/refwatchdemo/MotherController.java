package de.hannesstruss.refwatchdemo;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.RefWatchingController;

import butterknife.BindView;
import butterknife.OnClick;

public class MotherController extends RefWatchingController {
  @BindView(R.id.mother_container) ViewGroup motherContainer;

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.hs_controller_mother, container, false);
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
                               @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);

    if (changeType == ControllerChangeType.PUSH_ENTER) {
      Router childRouter = getChildRouter(motherContainer, null);
      if (!childRouter.hasRootController()) {
        childRouter.setRoot(RouterTransaction.with(new SimpleChildController(0xFFFFCCCC)));
      }
    }
  }

  @OnClick(R.id.btn_child_1) void onChild1Clicked() {
    Router childRouter = getChildRouter(motherContainer, null);
    childRouter.setRoot(RouterTransaction.with(new SimpleChildController(0xFFFFCCCC))
        .popChangeHandler(new FadeChangeHandler())
        .pushChangeHandler(new FadeChangeHandler()));
  }


  @OnClick(R.id.btn_child_2) void onChild2Clicked() {
    Router childRouter = getChildRouter(motherContainer, null);
    childRouter.setRoot(RouterTransaction.with(new SimpleChildController(0xFFCCFFCC))
        .popChangeHandler(new FadeChangeHandler())
        .pushChangeHandler(new FadeChangeHandler()));
  }
}
