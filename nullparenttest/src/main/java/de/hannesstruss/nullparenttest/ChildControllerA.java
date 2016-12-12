package de.hannesstruss.nullparenttest;

import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;

public class ChildControllerA extends Controller {
  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    TextView view = new TextView(inflater.getContext());
    view.setGravity(Gravity.CENTER);
    view.setText("Tap me!");
    view.setBackgroundColor(0xFFCCFFCC); // green

    view.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        getRouter().pushController(RouterTransaction.with(new ChildControllerB()));
      }
    });

    return view;
  }
}
