package thegrid.io.viewpagerinitdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

public class MainActivity extends AppCompatActivity {

  private Router router;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ViewGroup container = (ViewGroup) findViewById(R.id.container);

    router = Conductor.attachRouter(this, container, savedInstanceState);

    if (!router.hasRootController()) {
      router.setRoot(RouterTransaction.with(new MotherController()));
    }
  }

  @Override public void onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed();
    }
  }
}
