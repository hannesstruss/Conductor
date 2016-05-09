package com.bluelinelabs.conductor.demo.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

    @Bind({R.id.container_0, R.id.container_1, R.id.container_2}) ViewGroup[] mChildContainers;
    @Bind(R.id.bottom_navigation) AHBottomNavigation mBottomNavigation;
    ViewGroup mVisibleContainer;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_bottom_navigation_parent, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        mBottomNavigation.addItem(new AHBottomNavigationItem(R.string.groups, R.drawable.ic_group_black_24dp, R.color.blue_300));
        mBottomNavigation.addItem(new AHBottomNavigationItem(R.string.places, R.drawable.ic_place_black_24dp, R.color.brown_300));
        mBottomNavigation.addItem(new AHBottomNavigationItem(R.string.favorites, R.drawable.ic_favorite_black_24dp, R.color.red_300));

        mBottomNavigation.setOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, boolean wasSelected) {
                final ViewGroup oldContainer = mVisibleContainer;
                mVisibleContainer = mChildContainers[position];
                mVisibleContainer.setVisibility(View.VISIBLE);

                AnimatorSet containerAnimations = new AnimatorSet();
                containerAnimations.play(ObjectAnimator.ofFloat(mVisibleContainer, View.ALPHA, 1));
                if (oldContainer != null) {
                    containerAnimations.play(ObjectAnimator.ofFloat(oldContainer, View.ALPHA, 0));
                    containerAnimations.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            oldContainer.setVisibility(View.GONE);
                        }
                    });
                }
                containerAnimations.start();

                Router childRouter = getChildRouter(mChildContainers[position], null).setPopLastView(false);
                if (!childRouter.hasRootController()) {
                    childRouter.setRoot(RouterTransaction.builder(new NavigationDemoController(0)).build());
                }
            }
        });

        mBottomNavigation.setColored(true);
        mBottomNavigation.setCurrentItem(0);
    }
}
