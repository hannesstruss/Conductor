package com.bluelinelabs.conductor.internal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ActivityHostedRouter;
import com.bluelinelabs.conductor.Router;

import java.util.HashMap;
import java.util.Map;

public class LifecycleHandler extends Fragment implements ActivityLifecycleCallbacks {

    private static final String FRAGMENT_TAG = "LifecycleHandler";

    private static final String KEY_PERMISSION_REQUEST_CODES = "LifecycleHandler.permissionRequests";
    private static final String KEY_ACTIVITY_REQUEST_CODES = "LifecycleHandler.activityRequests";

    private Activity mActivity;
    private boolean mHasRegisteredCallbacks;

    private SparseArray<String> mPermissionRequestMap = new SparseArray<>();
    private SparseArray<String> mActivityRequestMap = new SparseArray<>();

    private final Map<Integer, ActivityHostedRouter> mRouterMap = new HashMap<>();

    public LifecycleHandler() {
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    private static LifecycleHandler findInActivity(Activity activity) {
        LifecycleHandler lifecycleHandler = (LifecycleHandler)activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (lifecycleHandler != null) {
            lifecycleHandler.registerActivityListener(activity);
        }
        return lifecycleHandler;
    }

    public static LifecycleHandler install(Activity activity) {
        LifecycleHandler lifecycleHandler = findInActivity(activity);
        if (lifecycleHandler == null) {
            lifecycleHandler = new LifecycleHandler();
            activity.getFragmentManager().beginTransaction().add(lifecycleHandler, FRAGMENT_TAG).commit();
        }
        lifecycleHandler.registerActivityListener(activity);
        return lifecycleHandler;
    }

    public Router getRouter(ViewGroup container, Bundle savedInstanceState) {
        ActivityHostedRouter router = mRouterMap.get(getRouterHashKey(container));
        if (router == null) {
            router = new ActivityHostedRouter();
            router.setHost(this, container);

            if (savedInstanceState != null) {
                router.restoreInstanceState(savedInstanceState);
            }
            mRouterMap.put(getRouterHashKey(container), router);
        } else {
            router.setHost(this, container);
        }

        return router;
    }

    public Activity getLifecycleActivity() {
        return mActivity;
    }

    private static int getRouterHashKey(ViewGroup viewGroup) {
        return viewGroup.getId();
    }

    private void registerActivityListener(Activity activity) {
        mActivity = activity;

        if (!mHasRegisteredCallbacks) {
            mHasRegisteredCallbacks = true;
            activity.getApplication().registerActivityLifecycleCallbacks(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            StringSparseArrayParceler permissionParcel = savedInstanceState.getParcelable(KEY_PERMISSION_REQUEST_CODES);
            mPermissionRequestMap = permissionParcel != null ? permissionParcel.getStringSparseArray() : null;

            StringSparseArrayParceler activityParcel = savedInstanceState.getParcelable(KEY_ACTIVITY_REQUEST_CODES);
            mActivityRequestMap = activityParcel != null ? activityParcel.getStringSparseArray() : null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_PERMISSION_REQUEST_CODES, new StringSparseArrayParceler(mPermissionRequestMap));
        outState.putParcelable(KEY_ACTIVITY_REQUEST_CODES, new StringSparseArrayParceler(mActivityRequestMap));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mActivity != null) {
            mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);

            for (Router router : mRouterMap.values()) {
                router.onActivityDestroyed(mActivity);
            }

            mActivity = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String instanceId = mActivityRequestMap.get(requestCode);
        if (instanceId != null) {
            for (Router router : mRouterMap.values()) {
                router.onActivityResult(instanceId, requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String instanceId = mPermissionRequestMap.get(requestCode);
        if (instanceId != null) {
            for (Router router : mRouterMap.values()) {
                router.onRequestPermissionsResult(instanceId, requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        for (Router router : mRouterMap.values()) {
            Boolean handled = router.handleRequestedPermission(permission);
            if (handled != null) {
                return handled;
            }
        }
        return super.shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        for (Router router : mRouterMap.values()) {
            router.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        for (Router router : mRouterMap.values()) {
            router.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        for (Router router : mRouterMap.values()) {
            if (router.onOptionsItemSelected(item)) {
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void registerForActivityResult(String instanceId, int requestCode) {
        mActivityRequestMap.put(requestCode, instanceId);
    }

    public void unregisterForActivityResults(String instanceId) {
        for (int i = mActivityRequestMap.size() - 1; i >= 0; i--) {
            if (instanceId.equals(mActivityRequestMap.get(mActivityRequestMap.keyAt(i)))) {
                mActivityRequestMap.removeAt(i);
            }
        }
    }

    public void startActivityForResult(String instanceId, Intent intent, int requestCode) {
        registerForActivityResult(instanceId, requestCode);
        startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(String instanceId, Intent intent, int requestCode, Bundle options) {
        registerForActivityResult(instanceId, requestCode);
        startActivityForResult(intent, requestCode, options);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(String instanceId, String[] permissions, int requestCode) {
        mPermissionRequestMap.put(requestCode, instanceId);
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (mActivity == null && findInActivity(activity) == LifecycleHandler.this) {
            mActivity = activity;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (mActivity == activity) {
            for (Router router : mRouterMap.values()) {
                router.onActivityStarted(activity);
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (mActivity == activity) {
            for (Router router : mRouterMap.values()) {
                router.onActivityResumed(activity);
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (mActivity == activity) {
            for (Router router : mRouterMap.values()) {
                router.onActivityPaused(activity);
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (mActivity == activity) {
            for (Router router : mRouterMap.values()) {
                router.onActivityStopped(activity);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (mActivity == activity) {
            for (Router router : mRouterMap.values()) {
                router.saveInstanceState(outState);
            }
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) { }
}
