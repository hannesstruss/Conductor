package com.bluelinelabs.conductor.demo.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation.OnTabSelectedListener;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.Bind;

public class BottomNavigationParentController extends BaseController {

    private static final String KEY_CURRENT_TAB = "BottomNavigationParentController.currentTab";

    @Bind({R.id.container_0, R.id.container_1, R.id.container_2}) ViewGroup[] mChildContainers;
    @Bind(R.id.bottom_navigation) AHBottomNavigation mBottomNavigation;
    ViewGroup mVisibleContainer;

    private int mCurrentTab;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_bottom_navigation_parent, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        setupBottomNavigation();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CURRENT_TAB, mCurrentTab);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentTab = savedInstanceState.getInt(KEY_CURRENT_TAB);
    }

    @Override
    protected void onDestroyView(View view) {
        super.onDestroyView(view);

        mVisibleContainer = null;
    }

    @Override
    public boolean handleBack() {
        return getChildRouter(mChildContainers[mCurrentTab], null).handleBack();
    }

    private void setupBottomNavigation() {
        mBottomNavigation.addItem(new AHBottomNavigationItem(R.string.groups, R.drawable.ic_group_black_24dp, R.color.blue_300));
        mBottomNavigation.addItem(new AHBottomNavigationItem(R.string.places, R.drawable.ic_place_black_24dp, R.color.brown_300));
        mBottomNavigation.addItem(new AHBottomNavigationItem(R.string.favorites, R.drawable.ic_favorite_black_24dp, R.color.red_300));

        mBottomNavigation.setOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, boolean wasSelected) {
                onNavTabSelected(position);
            }
        });

        mBottomNavigation.setColored(true);
        onNavTabSelected(mCurrentTab);
    }

    private void onNavTabSelected(int position) {
        if (mCurrentTab != position || mVisibleContainer == null) {
            mCurrentTab = position;

            final ViewGroup oldContainer = mVisibleContainer;
            mVisibleContainer = mChildContainers[position];
            mVisibleContainer.setVisibility(View.VISIBLE);

            if (oldContainer != null) {
                AnimatorSet containerAnimations = new AnimatorSet();
                containerAnimations.play(ObjectAnimator.ofFloat(mVisibleContainer, View.ALPHA, 1));
                containerAnimations.play(ObjectAnimator.ofFloat(oldContainer, View.ALPHA, 0));
                containerAnimations.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        oldContainer.setVisibility(View.GONE);
                    }
                });
                containerAnimations.start();
            } else {
                mVisibleContainer.setAlpha(1);
            }

            Router childRouter = getChildRouter(mChildContainers[position], null).setPopsLastView(false);
            if (!childRouter.hasRootController()) {
                childRouter.setRoot(RouterTransaction.builder(new NavigationDemoController(0)).build());
            }
        }
    }
}
