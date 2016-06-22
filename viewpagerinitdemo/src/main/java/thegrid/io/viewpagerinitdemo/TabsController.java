package thegrid.io.viewpagerinitdemo;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.support.ControllerPagerAdapter;

public class TabsController extends Controller {
  enum Tab {
    A(0xFFFFCCCC), B(0xFFCCFFCC), C(0xFFCCCCFF);
    final int color;

    Tab(int color) {
      this.color = color;
    }
  }

  @NonNull @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.controller_tabs, container, false);


    ViewPager vp = (ViewPager) view.findViewById(R.id.view_pager);
    vp.setAdapter(new TabsAdapter(this));

    TabLayout ts = (TabLayout) view.findViewById(R.id.tab_layout);
    ts.setupWithViewPager(vp);
    return view;
  }

  static class TabsAdapter extends ControllerPagerAdapter {

    public TabsAdapter(Controller host) {
      super(host, false);
    }

    @Override public Controller getItem(int position) {
      Log.d("Test", "getItem: " + position);
      Tab tab = Tab.values()[position];
      return new TabController(tab.color);
    }

    @Override public int getCount() {
      return Tab.values().length;
    }

    @Override public CharSequence getPageTitle(int position) {
      return "Tab " + position;
    }
  }
}
