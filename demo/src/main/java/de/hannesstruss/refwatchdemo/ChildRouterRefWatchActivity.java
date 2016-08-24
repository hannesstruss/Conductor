package de.hannesstruss.refwatchdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.demo.R;

public class ChildRouterRefWatchActivity extends AppCompatActivity {
  private Router router;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.hs_activity_refwatch_example);

    router = Conductor.attachRouter(this, (ViewGroup) findViewById(R.id.activity_container), savedInstanceState);
    if (!router.hasRootController()) {
      router.pushController(RouterTransaction.with(new MotherController()));
    }
  }
}
