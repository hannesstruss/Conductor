package de.hannesstruss.nullparenttest;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;

import java.util.List;

public class ChildControllerB extends Controller {
  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
//    Debug.waitForDebugger();
    log("onCreateView");

    View view = new View(inflater.getContext());
    view.setBackgroundColor(0xFFFFCCCC); // red
    return view;
  }

  @Override protected void onAttach(@NonNull View view) {
    log("onAttach pre super");
    super.onAttach(view);
    log("onAttach post super");
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
                               @NonNull ControllerChangeType changeType) {
    log("onChangeEnded pre super");
    super.onChangeEnded(changeHandler, changeType);
    log("onChangeEnded post super");
  }

  @Override protected void onActivityStarted(@NonNull Activity activity) {
    log("onActivityStarted pre super");
    super.onActivityStarted(activity);
    log("onActivityStarted post super");
  }

  @Override protected void onActivityResumed(@NonNull Activity activity) {
    log("onActivityResumed pre super");
    super.onActivityResumed(activity);
    log("onActivityResumed post super");
  }

  @Override protected void onRestoreViewState(@NonNull View view, @NonNull Bundle savedViewState) {
    log("onRestoreViewState pre super");
    super.onRestoreViewState(view, savedViewState);
    log("onRestoreViewState post super");
  }

  @Override protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    log("onRestoreInstanceState pre super");
    super.onRestoreInstanceState(savedInstanceState);
    log("onRestoreInstanceState post super");
  }

  private void log(String fromMethod) {
    List<RouterTransaction> backstack = getRouter().getBackstack();
    Log.d("ChildController", String.format("In %s getParentController is: %s\npreviousController.getParentController is: %s",
        fromMethod,
        getParentController() == null ? "null" : "not null",
        backstack.get(backstack.size() - 1).controller().getParentController() == null ? "null" : "not null"
    ));
    Log.d("ChildController", "---");
  }
}
