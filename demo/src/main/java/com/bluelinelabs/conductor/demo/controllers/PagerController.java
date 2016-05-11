package com.bluelinelabs.conductor.demo.controllers;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.support.ControllerPagerAdapter;

import butterknife.Bind;

public class PagerController extends BaseController {

    private int[] PAGE_COLORS = new int[]{R.color.green_300, R.color.cyan_300, R.color.deep_purple_300, R.color.lime_300, R.color.red_300};

    @Bind(R.id.tab_layout) TabLayout mTabLayout;
    @Bind(R.id.view_pager) ViewPager mViewPager;

    private final ControllerPagerAdapter mPagerAdapter;

    public PagerController() {
        mPagerAdapter = new ControllerPagerAdapter(this) {
            @Override
            public Controller getItem(int position) {
                return new ChildController(String.format("Child #%d (Swipe to see more)", position), PAGE_COLORS[position], true);
            }

            @Override
            public int getCount() {
                return PAGE_COLORS.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "Page " + position;
            }
        };
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        mTabLayout.setTabsFromPagerAdapter(mPagerAdapter);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab) { }

            @Override
            public void onTabReselected(Tab tab) { }
        });
    }

    @Override
    protected void onDestroyView(View view) {
        mViewPager.setAdapter(null);
        super.onDestroyView(view);
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_pager, container, false);
    }

    @Override
    protected String getTitle() {
        return "ViewPager Demo";
    }

}
