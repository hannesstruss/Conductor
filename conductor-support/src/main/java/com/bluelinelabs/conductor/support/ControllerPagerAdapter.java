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

    private final Controller mHost;
    private boolean mSavesState;
    private SparseArray<Bundle> mSavedPages = new SparseArray<>();

    /**
     * Creates a new ControllerPagerAdapter using the passed host.
     */
    public ControllerPagerAdapter(Controller host, boolean saveControllerState) {
        mHost = host;
        mSavesState = saveControllerState;
    }

    /**
     * Return the Controller associated with a specified position.
     */
    public abstract Controller getItem(int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final String name = makeControllerName(container.getId(), getItemId(position));

        Router router = mHost.getChildRouter(container, name);
        if (mSavesState && !router.hasRootController()) {
            Bundle routerSavedState = mSavedPages.get(position);

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

        if (mSavesState) {
            Bundle savedState = new Bundle();
            router.saveInstanceState(savedState);
            mSavedPages.put(position, savedState);
        }

        mHost.removeChildRouter(router);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Controller)object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_SAVES_STATE, mSavesState);
        bundle.putSparseParcelableArray(KEY_SAVED_PAGES, mSavedPages);
        return bundle;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Bundle bundle = (Bundle)state;
        if (state != null) {
            mSavesState = bundle.getBoolean(KEY_SAVES_STATE, false);
            mSavedPages = bundle.getSparseParcelableArray(KEY_SAVED_PAGES);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    private static String makeControllerName(int viewId, long id) {
        return viewId + ":" + id;
    }

}