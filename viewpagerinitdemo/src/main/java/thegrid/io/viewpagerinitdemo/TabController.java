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

    view.setBackgroundColor(getArgs().getInt(EXTRA_COLOR));

    view.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        getParentController().getParentController().getRouter()
            .pushController(RouterTransaction.with(new OtherController())
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
      }
    });
    return view;
  }
}
