package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bluelinelabs.conductor.internal.ClassUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ControllerChangeHandlers are responsible for swapping the View for one Controller to the View
 * of another. They can be useful for performing animations and transitions between Controllers. Several
 * default ControllerChangeHandlers are included.
 */
public abstract class ControllerChangeHandler {

    private static final String KEY_CLASS_NAME = "ControllerChangeHandler.className";
    private static final String KEY_SAVED_STATE = "ControllerChangeHandler.savedState";

    private static final Map<String, ControllerChangeHandler> inProgressPushHandlers = new HashMap<>();

    /**
     * Responsible for swapping Views from one Controller to another.
     *
     * @param container The container these Views are hosted in.
     * @param from The previous View in the container, if any.
     * @param to The next View that should be put in the container, if any.
     * @param isPush True if this is a push transaction, false if it's a pop.
     * @param changeListener This listener must be called when any transitions or animations are completed.
     */
    public abstract void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener);

    public ControllerChangeHandler() {
        ensureDefaultConstructor();
    }

    /**
     * Saves any data about this handler to a Bundle in case the application is killed.
     *
     * @param bundle The Bundle into which data should be stored.
     */
    public void saveToBundle(@NonNull Bundle bundle) { }

    /**
     * Restores data that was saved in the {@link #saveToBundle(Bundle bundle)} method.
     *
     * @param bundle The bundle that has data to be restored
     */
    public void restoreFromBundle(@NonNull Bundle bundle) { }

    /**
     * Will be called on change handlers that push a controller if the controller being pushed is
     * popped before it has completed.
     *
     * @param newHandler the change handler that has caused this push to be aborted
     * @param newTop the controller that will now be at the top of the backstack
     */
    public void onAbortPush(@NonNull ControllerChangeHandler newHandler, Controller newTop) { }

    final Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CLASS_NAME, getClass().getName());

        Bundle savedState = new Bundle();
        saveToBundle(savedState);
        bundle.putBundle(KEY_SAVED_STATE, savedState);

        return bundle;
    }

    private void ensureDefaultConstructor() {
        try {
            getClass().getConstructor();
        } catch (Exception e) {
            throw new RuntimeException(getClass() + " does not have a default constructor.");
        }
    }

    public static ControllerChangeHandler fromBundle(Bundle bundle) {
        if (bundle != null) {
            String className = bundle.getString(KEY_CLASS_NAME);
            ControllerChangeHandler changeHandler = ClassUtils.newInstance(className);
            //noinspection ConstantConditions
            changeHandler.restoreFromBundle(bundle.getBundle(KEY_SAVED_STATE));
            return changeHandler;
        } else {
            return null;
        }
    }

    public static void executeChange(final Controller to, final Controller from, boolean isPush, ViewGroup container, ControllerChangeHandler inHandler) {
        executeChange(to, from, isPush, container, inHandler, new ArrayList<ControllerChangeListener>());
    }

    public static void executeChange(final Controller to, final Controller from, final boolean isPush, final ViewGroup container, final ControllerChangeHandler inHandler, @NonNull final List<ControllerChangeListener> listeners) {
        if (container != null) {
            final ControllerChangeHandler handler = inHandler != null ? inHandler : new SimpleSwapChangeHandler();

            if (isPush) {
                inProgressPushHandlers.put(to.getInstanceId(), handler);
            } else if (from != null) {
                ControllerChangeHandler handlerForPush = inProgressPushHandlers.get(from.getInstanceId());
                if (handlerForPush != null) {
                    handlerForPush.onAbortPush(handler, to);
                    inProgressPushHandlers.remove(from.getInstanceId());
                }
            }

            for (ControllerChangeListener listener : listeners) {
                listener.onChangeStarted(to, from, isPush, container, inHandler);
            }

            final ControllerChangeType toChangeType = isPush ? ControllerChangeType.PUSH_ENTER : ControllerChangeType.POP_ENTER;
            final ControllerChangeType fromChangeType = isPush ? ControllerChangeType.PUSH_EXIT : ControllerChangeType.POP_EXIT;

            final View toView;
            if (to != null) {
                toView = to.inflate(container);
                to.changeStarted(handler, toChangeType);
            } else {
                toView = null;
            }

            final View fromView;
            if (from != null) {
                fromView = from.getView();
                from.changeStarted(handler, fromChangeType);
            } else {
                fromView = null;
            }

            handler.performChange(container, fromView, toView, isPush, new ControllerChangeCompletedListener() {
                @Override
                public void onChangeCompleted() {

                    if (from != null) {
                        from.changeEnded(handler, fromChangeType);
                    }

                    if (to != null) {
                        inProgressPushHandlers.remove(to.getInstanceId());
                        to.changeEnded(handler, toChangeType);
                    }

                    for (ControllerChangeListener listener : listeners) {
                        listener.onChangeCompleted(to, from, isPush, container, inHandler);
                    }
                }
            });
        }
    }

    /**
     * A listener interface useful for allowing external classes to be notified of change events.
     */
    public interface ControllerChangeListener {
        /**
         * Called when a {@link ControllerChangeHandler} has started changing {@link Controller}s
         *
         * @param to The new Controller
         * @param from The old Controller
         * @param isPush True if this is a push operation, or false if it's a pop.
         * @param container The containing ViewGroup
         * @param handler The change handler being used.
         */
        void onChangeStarted(Controller to, Controller from, boolean isPush, ViewGroup container, ControllerChangeHandler handler);

        /**
         * Called when a {@link ControllerChangeHandler} has completed changing {@link Controller}s
         * @param to The new Controller
         * @param from The old Controller
         * @param isPush True if this was a push operation, or false if it's a pop.
         * @param container The containing ViewGroup
         * @param handler The change handler that was used.
         */
        void onChangeCompleted(Controller to, Controller from, boolean isPush, ViewGroup container, ControllerChangeHandler handler);
    }

    /**
     * A simplified listener for being notified when the change is complete. This MUST be called by any custom
     * ControllerChangeHandlers in order to ensure that {@link Controller}s will be notified of this change.
     */
    public interface ControllerChangeCompletedListener {
        /**
         * Called when the change is complete.
         */
        void onChangeCompleted();
    }

}
