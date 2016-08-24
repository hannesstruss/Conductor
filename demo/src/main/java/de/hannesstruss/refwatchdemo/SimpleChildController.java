package de.hannesstruss.refwatchdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.demo.controllers.base.RefWatchingController;

public class SimpleChildController extends RefWatchingController {
  private final int color;

  public SimpleChildController(int color) {
    this.color = color;
  }

  public SimpleChildController(Bundle bundle) {
    throw new AssertionError("Don't need this for this example");
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = new View(inflater.getContext());
    view.setBackgroundColor(color);
    return view;
  }
}
