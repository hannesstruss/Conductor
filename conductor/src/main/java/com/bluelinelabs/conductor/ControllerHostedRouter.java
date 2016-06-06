package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;

public class ControllerHostedRouter extends Router {

    private final String KEY_HOST_ID = "ControllerHostedRouter.hostId";
    private final String KEY_TAG = "ControllerHostedRouter.tag";

    private Controller mHostController;

    @IdRes private int mHostId;
    private String mTag;

    public ControllerHostedRouter() { }

    public ControllerHostedRouter(int hostId, String tag) {
        mHostId = hostId;
        mTag = tag;
    }

    public final void setHost(@NonNull Controller controller, @NonNull ViewGroup container) {
        if (mHostController != controller || mContainer != container) {
            if (mContainer != null && mContainer instanceof ControllerChangeListener) {
                removeChangeListener((ControllerChangeListener)mContainer);
            }

            if (container instanceof ControllerChangeListener) {
                addChangeListener((ControllerChangeListener)container);
            }

            mHostController = controller;
            mContainer = container;
        }
    }

    @Override
    public Activity getActivity() {
        return mHostController != null ? mHostController.getActivity() : null;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        super.onActivityDestroyed(activity);

        mHostController = null;
        mContainer = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHostController != null && mHostController.getRouter() != null) {
            mHostController.getRouter().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void invalidateOptionsMenu() {
        if (mHostController != null && mHostController.getRouter() != null) {
            mHostController.getRouter().invalidateOptionsMenu();
        }
    }

    @Override
    void startActivity(Intent intent) {
        if (mHostController != null && mHostController.getRouter() != null) {
            mHostController.getRouter().startActivity(intent);
        }
    }

    @Override
    void startActivityForResult(String instanceId, Intent intent, int requestCode) {
        if (mHostController != null && mHostController.getRouter() != null) {
            mHostController.getRouter().startActivityForResult(instanceId, intent, requestCode);
        }
    }

    @Override
    void startActivityForResult(String instanceId, Intent intent, int requestCode, Bundle options) {
        if (mHostController != null && mHostController.getRouter() != null) {
            mHostController.getRouter().startActivityForResult(instanceId, intent, requestCode, options);
        }
    }

    @Override
    void registerForActivityResult(String instanceId, int requestCode) {
        if (mHostController != null && mHostController.getRouter() != null) {
            mHostController.getRouter().registerForActivityResult(instanceId, requestCode);
        }
    }

    @Override
    void unregisterForActivityResults(String instanceId) {
        if (mHostController != null && mHostController.getRouter() != null) {
            mHostController.getRouter().unregisterForActivityResults(instanceId);
        }
    }

    @Override
    void requestPermissions(String instanceId, String[] permissions, int requestCode) {
        if (mHostController != null && mHostController.getRouter() != null) {
            mHostController.getRouter().requestPermissions(instanceId, permissions, requestCode);
        }
    }

    @Override
    boolean hasHost() {
        return mHostController != null;
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        super.saveInstanceState(outState);

        outState.putInt(KEY_HOST_ID, mHostId);
        outState.putString(KEY_TAG, mTag);
    }

    @Override
    public void restoreInstanceState(Bundle savedInstanceState) {
        super.restoreInstanceState(savedInstanceState);

        mHostId = savedInstanceState.getInt(KEY_HOST_ID);
        mTag = savedInstanceState.getString(KEY_TAG);
    }

    @Override
    void setControllerRouter(Controller controller) {
        super.setControllerRouter(controller);
        controller.setParentController(mHostController);
    }

    @Override
    void pushToBackstack(@NonNull RouterTransaction entry) {
        super.pushToBackstack(entry);

        mHostController.getRouter().onChildControllerPushed(entry.controller);
    }

    @Override
    void onChildControllerPushed(Controller controller) {
        super.onChildControllerPushed(controller);

        mHostController.getRouter().onChildControllerPushed(controller);
    }

    public int getHostId() {
        return mHostId;
    }

    public String getTag() {
        return mTag;
    }
}
