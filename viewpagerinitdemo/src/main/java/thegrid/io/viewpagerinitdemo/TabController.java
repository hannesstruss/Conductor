package thegrid.io.viewpagerinitdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;

public class TabController extends Controller {
  static int StaticIndex = 0;
  static final int[] COLORS = {
      0xFF000099,
      0xFF009900,
      0xFF990000,
      0xFF0099FF
  };
  private static final String EXTRA_INDEX = "EXTRA_INDEX";

  public TabController(int color) {
    this(bundle(color));
  }

  public TabController(Bundle args) {
    super(args);
  }

  public static int newIndex() {
    return StaticIndex++;
  }

  static Bundle bundle(int index) {
    Bundle bundle = new Bundle();
    bundle.putInt(EXTRA_INDEX, index);
    return bundle;
  }

  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.controller_tab, container, false);

    final int index = getIndex();
    view.setBackgroundColor(COLORS[index % COLORS.length]);

    ((TextView) view.findViewById(R.id.txt_index)).setText(String.valueOf(index));

    view.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        getRouter()
            .pushController(RouterTransaction.with(new TabController(newIndex()))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
      }
    });
    return view;
  }

  private int getIndex() {return getArgs().getInt(EXTRA_INDEX);}

  @Override public String toString() {
    return String.format("TabController[%s]", getIndex());
  }
}
