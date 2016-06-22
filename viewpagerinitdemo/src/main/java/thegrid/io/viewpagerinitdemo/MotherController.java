package thegrid.io.viewpagerinitdemo;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;

public class MotherController extends Controller {
  private ViewGroup container;

  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.controller_mother, container, false);

    this.container = (ViewGroup) view.findViewById(R.id.mother_container);

    return view;
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
                               @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);

    if (changeType == ControllerChangeType.PUSH_ENTER) {
      getChildRouter(container, null).pushController(RouterTransaction.with(new TabsController()));
    }
  }
}
