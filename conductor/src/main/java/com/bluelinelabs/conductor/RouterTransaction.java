package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Metadata used for adding {@link Controller}s to a {@link Router}.
 */
public class RouterTransaction {

    private static final String KEY_VIEW_CONTROLLER_BUNDLE = "RouterTransaction.controller.bundle";
    private static final String KEY_PUSH_TRANSITION = "RouterTransaction.pushControllerChangeHandler";
    private static final String KEY_POP_TRANSITION = "RouterTransaction.popControllerChangeHandler";
    private static final String KEY_TAG = "RouterTransaction.tag";
    private static final String KEY_ATTACHED_TO_ROUTER = "RouterTransaction.attachedToRouter";

    @NonNull final Controller controller;
    private String tag;

    private ControllerChangeHandler mPushControllerChangeHandler;
    private ControllerChangeHandler mPopControllerChangeHandler;
    private boolean mAttachedToRouter;

    public static RouterTransaction with(@NonNull Controller controller) {
        return new RouterTransaction(controller);
    }

    private RouterTransaction(@NonNull Controller controller) {
        this.controller = controller;
    }

    RouterTransaction(@NonNull Bundle bundle) {
        controller = Controller.newInstance(bundle.getBundle(KEY_VIEW_CONTROLLER_BUNDLE));
        mPushControllerChangeHandler = ControllerChangeHandler.fromBundle(bundle.getBundle(KEY_PUSH_TRANSITION));
        mPopControllerChangeHandler = ControllerChangeHandler.fromBundle(bundle.getBundle(KEY_POP_TRANSITION));
        tag = bundle.getString(KEY_TAG);
        mAttachedToRouter = bundle.getBoolean(KEY_ATTACHED_TO_ROUTER);
    }

    void onAttachedToRouter() {
        mAttachedToRouter = true;
    }

    String tag() {
        return tag;
    }

    public RouterTransaction tag(String tag) {
        if (!mAttachedToRouter) {
            this.tag = tag;
            return this;
        } else {
            throw new RuntimeException(getClass().getSimpleName() + "s can not be modified after being added to a Router.");
        }
    }

    ControllerChangeHandler pushChangeHandler() {
        ControllerChangeHandler handler = controller.getOverriddenPushHandler();
        if (handler == null) {
            handler = mPushControllerChangeHandler;
        }
        return handler;
    }

    public RouterTransaction pushChangeHandler(ControllerChangeHandler handler) {
        if (!mAttachedToRouter) {
            mPushControllerChangeHandler = handler;
            return this;
        } else {
            throw new RuntimeException(getClass().getSimpleName() + "s can not be modified after being added to a Router.");
        }
    }

    ControllerChangeHandler popChangeHandler() {
        ControllerChangeHandler handler = controller.getOverriddenPopHandler();
        if (handler == null) {
            handler = mPopControllerChangeHandler;
        }
        return handler;
    }

    public RouterTransaction popChangeHandler(ControllerChangeHandler handler) {
        if (!mAttachedToRouter) {
            mPopControllerChangeHandler = handler;
            return this;
        } else {
            throw new RuntimeException(getClass().getSimpleName() + "s can not be modified after being added to a Router.");
        }
    }

    /**
     * Used to serialize this transaction into a Bundle
     */
    public Bundle saveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putBundle(KEY_VIEW_CONTROLLER_BUNDLE, controller.saveInstanceState());

        if (mPushControllerChangeHandler != null) {
            bundle.putBundle(KEY_PUSH_TRANSITION, mPushControllerChangeHandler.toBundle());
        }
        if (mPopControllerChangeHandler != null) {
            bundle.putBundle(KEY_POP_TRANSITION, mPopControllerChangeHandler.toBundle());
        }

        bundle.putString(KEY_TAG, tag);
        bundle.putBoolean(KEY_ATTACHED_TO_ROUTER, mAttachedToRouter);

        return bundle;
    }

}