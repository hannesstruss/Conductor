package thegrid.io.viewpagerinitdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;

public class TabController extends Controller {
  static final int[] COLORS = {
      0xFF000099,
      0xFF009900,
      0xFF990000,
      0xFF0099FF
  };
  private static final String EXTRA_COLOR = "EXTRA_COLOR";

  public TabController(int color) {
    this(bundle(color));
  }

  public TabController(Bundle args) {
    super(args);
  }

  static Bundle bundle(int color) {
    Bundle bundle = new Bundle();
    bundle.putInt(EXTRA_COLOR, color);
    return bundle;
  }

  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.controller_tab, container, false);

    final int color = getArgs().getInt(EXTRA_COLOR);
    view.setBackgroundColor(COLORS[color % COLORS.length]);

    view.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        getRouter()
            .pushController(RouterTransaction.with(new TabController(color + 1))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
      }
    });
    return view;
  }
}
