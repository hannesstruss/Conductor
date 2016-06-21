package com.bluelinelabs.conductor;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener;

/**
 * A FrameLayout implementation that can be used to block user interactions while
 * {@link ControllerChangeHandler}s are performing changes. It is not required to use this
 * ViewGroup, but it can be helpful.
 */
public class ChangeHandlerFrameLayout extends FrameLayout implements ControllerChangeListener {

    private int inProgressTransactionCount;

    public ChangeHandlerFrameLayout(Context context) {
        super(context);
    }

    public ChangeHandlerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangeHandlerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChangeHandlerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return (inProgressTransactionCount > 0) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onChangeStarted(Controller to, Controller from, boolean isPush, ViewGroup container, ControllerChangeHandler handler) {
        inProgressTransactionCount++;
    }

    @Override
    public void onChangeCompleted(Controller to, Controller from, boolean isPush, ViewGroup container, ControllerChangeHandler handler) {
        inProgressTransactionCount--;
    }

}
