package de.hannesstruss.nullparenttest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

public class MainActivity extends AppCompatActivity {
  private Router router;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ViewGroup root = new FrameLayout(this);
    setContentView(root);

    router = Conductor.attachRouter(this, root, savedInstanceState);
    if (!router.hasRootController()) {
      router.pushController(RouterTransaction.with(new ParentController()));
    }
  }
}
