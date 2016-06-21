package com.bluelinelabs.conductor.support;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

/**
 * An adapter for ViewPagers that will handle adding and removing Controllers
 */
public abstract class ControllerPagerAdapter extends PagerAdapter {

    private static final String KEY_SAVED_PAGES = "ControllerPagerAdapter.savedStates";
    private static final String KEY_SAVES_STATE = "ControllerPagerAdapter.savesState";

    private final Controller host;
    private boolean savesState;
    private SparseArray<Bundle> savedPages = new SparseArray<>();

    /**
     * Creates a new ControllerPagerAdapter using the passed host.
     */
    public ControllerPagerAdapter(Controller host, boolean saveControllerState) {
        this.host = host;
        savesState = saveControllerState;
    }

    /**
     * Return the Controller associated with a specified position.
     */
    public abstract Controller getItem(int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final String name = makeControllerName(container.getId(), getItemId(position));

        Router router = host.getChildRouter(container, name);
        if (savesState && !router.hasRootController()) {
            Bundle routerSavedState = savedPages.get(position);

            if (routerSavedState != null) {
                router.restoreInstanceState(routerSavedState);
            }
        }

        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(getItem(position))
                    .tag(name));
        } else {
            router.rebindIfNeeded();
        }

        return router.getControllerWithTag(name);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Router router = ((Controller)object).getRouter();

        if (savesState) {
            Bundle savedState = new Bundle();
            router.saveInstanceState(savedState);
            savedPages.put(position, savedState);
        }

        host.removeChildRouter(router);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Controller)object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_SAVES_STATE, savesState);
        bundle.putSparseParcelableArray(KEY_SAVED_PAGES, savedPages);
        return bundle;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Bundle bundle = (Bundle)state;
        if (state != null) {
            savesState = bundle.getBoolean(KEY_SAVES_STATE, false);
            savedPages = bundle.getSparseParcelableArray(KEY_SAVED_PAGES);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    private static String makeControllerName(int viewId, long id) {
        return viewId + ":" + id;
    }

}