package com.bluelinelabs.conductor.support;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;

/**
 * An adapter for ViewPagers that will handle adding and removing Controllers
 */
public abstract class ControllerPagerAdapter extends PagerAdapter {

    private final Controller mHost;

    /**
     * Creates a new ControllerPagerAdapter using the passed host.
     */
    public ControllerPagerAdapter(Controller host) {
        mHost = host;
    }

    /**
     * Return the Controller associated with a specified position.
     */
    public abstract Controller getItem(int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final String name = makeControllerName(container.getId(), getItemId(position));

        Router router = mHost.getChildRouter(container, name);
        if (!router.hasRootController()) {
            router.setRoot(getItem(position), name, new SimpleSwapChangeHandler());
        }

        return router.getControllerWithTag(name);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mHost.removeChildRouter(((Controller)object).getRouter());
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Controller)object).getView() == view;
    }

    public long getItemId(int position) {
        return position;
    }

    private static String makeControllerName(int viewId, long id) {
        return viewId + ":" + id;
    }

}