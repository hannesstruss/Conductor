package com.bluelinelabs.conductor.changehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;

/**
 * A base {@link ControllerChangeHandler} that facilitates using {@link android.transition.Transition}s to replace Controller Views.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public abstract class TransitionChangeHandler extends ControllerChangeHandler {

    private boolean canceled;

    /**
     * Should be overridden to return the Transition to use while replacing Views.
     *
     * @param container The container these Views are hosted in.
     * @param from The previous View in the container, if any.
     * @param to The next View that should be put in the container, if any.
     * @param isPush True if this is a push transaction, false if it's a pop.
     */
    @NonNull
    protected abstract Transition getTransition(@NonNull ViewGroup container, View from, View to, boolean isPush);

    @Override
    public void onAbortPush(@NonNull ControllerChangeHandler newHandler, Controller newTop) {
        super.onAbortPush(newHandler, newTop);

        canceled = true;
    }

    @Override
    public void performChange(@NonNull final ViewGroup container, View from, View to, boolean isPush, @NonNull final ControllerChangeCompletedListener changeListener) {
        if (canceled) {
            changeListener.onChangeCompleted();
            return;
        }

        Transition transition = getTransition(container, from, to, isPush);
        transition.addListener(new TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) { }

            @Override
            public void onTransitionEnd(Transition transition) {
                changeListener.onChangeCompleted();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                changeListener.onChangeCompleted();
            }

            @Override
            public void onTransitionPause(Transition transition) { }

            @Override
            public void onTransitionResume(Transition transition) { }
        });

        TransitionManager.beginDelayedTransition(container, transition);
        if (from != null) {
            container.removeView(from);
        }
        if (to != null) {
            container.addView(to);
        }
    }

}
