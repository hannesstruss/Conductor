package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;
import com.bluelinelabs.conductor.internal.LifecycleHandler;

import java.util.List;

public class ActivityHostedRouter extends Router {

    private LifecycleHandler mLifecycleHandler;

    public final void setHost(@NonNull LifecycleHandler lifecycleHandler, @NonNull ViewGroup container) {
        if (mLifecycleHandler != lifecycleHandler || mContainer != container) {
            if (mContainer != null && mContainer instanceof ControllerChangeListener) {
                removeChangeListener((ControllerChangeListener)mContainer);
            }

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener)container);
            }

            mLifecycleHandler = lifecycleHandler;
            mContainer = container;
        }
    }

    @Override
    public Activity getActivity() {
        return mLifecycleHandler != null ? mLifecycleHandler.getLifecycleActivity() : null;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        super.onActivityDestroyed(activity);
        mLifecycleHandler = null;
    }

    @Override
    public final void invalidateOptionsMenu() {
        if (mLifecycleHandler != null && mLifecycleHandler.getFragmentManager() != null) {
            mLifecycleHandler.getFragmentManager().invalidateOptionsMenu();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLifecycleHandler.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    void startActivity(Intent intent) {
        mLifecycleHandler.startActivity(intent);
    }

    @Override
    void startActivityForResult(String instanceId, Intent intent, int requestCode) {
        mLifecycleHandler.startActivityForResult(instanceId, intent, requestCode);
    }

    @Override
    void startActivityForResult(String instanceId, Intent intent, int requestCode, Bundle options) {
        mLifecycleHandler.startActivityForResult(instanceId, intent, requestCode, options);
    }

    @Override
    void registerForActivityResult(String instanceId, int requestCode) {
        mLifecycleHandler.registerForActivityResult(instanceId, requestCode);
    }

    @Override
    void unregisterForActivityResults(String instanceId) {
        mLifecycleHandler.unregisterForActivityResults(instanceId);
    }

    @Override
    void requestPermissions(String instanceId, @NonNull String[] permissions, int requestCode) {
        mLifecycleHandler.requestPermissions(instanceId, permissions, requestCode);
    }

    @Override
    boolean hasHost() {
        return mLifecycleHandler != null;
    }

    @Override
    List<Router> getSiblingRouters() {
        return mLifecycleHandler.getRouters();
    }
}
