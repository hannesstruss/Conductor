package com.bluelinelabs.conductor.internal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Fragment;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LifecycleHandler extends Fragment implements ActivityLifecycleCallbacks {

    private static final String FRAGMENT_TAG = "LifecycleHandler";

    private static final String KEY_PERMISSION_REQUEST_CODES = "LifecycleHandler.permissionRequests";
    private static final String KEY_ACTIVITY_REQUEST_CODES = "LifecycleHandler.activityRequests";

    private Activity activity;
    private boolean hasRegisteredCallbacks;
    private boolean destroyed;

    private SparseArray<String> permissionRequestMap = new SparseArray<>();
    private SparseArray<String> activityRequestMap = new SparseArray<>();

    private final Map<Integer, ActivityHostedRouter> routerMap = new HashMap<>();

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
        ActivityHostedRouter router = routerMap.get(getRouterHashKey(container));
        if (router == null) {
            router = new ActivityHostedRouter();
            router.setHost(this, container);

            if (savedInstanceState != null) {
                router.restoreInstanceState(savedInstanceState);
            }
            routerMap.put(getRouterHashKey(container), router);
        } else {
            router.setHost(this, container);
        }

        return router;
    }

    public List<Router> getRouters() {
        return new ArrayList<Router>(routerMap.values());
    }

    public Activity getLifecycleActivity() {
        return activity;
    }

    private static int getRouterHashKey(ViewGroup viewGroup) {
        return viewGroup.getId();
    }

    private void registerActivityListener(Activity activity) {
        this.activity = activity;

        if (!hasRegisteredCallbacks) {
            hasRegisteredCallbacks = true;
            activity.getApplication().registerActivityLifecycleCallbacks(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            StringSparseArrayParceler permissionParcel = savedInstanceState.getParcelable(KEY_PERMISSION_REQUEST_CODES);
            permissionRequestMap = permissionParcel != null ? permissionParcel.getStringSparseArray() : null;

            StringSparseArrayParceler activityParcel = savedInstanceState.getParcelable(KEY_ACTIVITY_REQUEST_CODES);
            activityRequestMap = activityParcel != null ? activityParcel.getStringSparseArray() : null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_PERMISSION_REQUEST_CODES, new StringSparseArrayParceler(permissionRequestMap));
        outState.putParcelable(KEY_ACTIVITY_REQUEST_CODES, new StringSparseArrayParceler(activityRequestMap));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (activity != null) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            destroyRouters();
            activity = null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        destroyed = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        destroyed = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        destroyRouters();
    }

    private void destroyRouters() {
        if (!destroyed) {
            destroyed = true;

            if (activity != null) {
                for (Router router : routerMap.values()) {
                    router.onActivityDestroyed(activity);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String instanceId = activityRequestMap.get(requestCode);
        if (instanceId != null) {
            for (Router router : routerMap.values()) {
                router.onActivityResult(instanceId, requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String instanceId = permissionRequestMap.get(requestCode);
        if (instanceId != null) {
            for (Router router : routerMap.values()) {
                router.onRequestPermissionsResult(instanceId, requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        for (Router router : routerMap.values()) {
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

        for (Router router : routerMap.values()) {
            router.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        for (Router router : routerMap.values()) {
            router.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        for (Router router : routerMap.values()) {
            if (router.onOptionsItemSelected(item)) {
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void registerForActivityResult(String instanceId, int requestCode) {
        activityRequestMap.put(requestCode, instanceId);
    }

    public void unregisterForActivityResults(String instanceId) {
        for (int i = activityRequestMap.size() - 1; i >= 0; i--) {
            if (instanceId.equals(activityRequestMap.get(activityRequestMap.keyAt(i)))) {
                activityRequestMap.removeAt(i);
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
        permissionRequestMap.put(requestCode, instanceId);
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (this.activity == null && findInActivity(activity) == LifecycleHandler.this) {
            this.activity = activity;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (this.activity == activity) {
            for (Router router : routerMap.values()) {
                router.onActivityStarted(activity);
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (this.activity == activity) {
            for (Router router : routerMap.values()) {
                router.onActivityResumed(activity);
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (this.activity == activity) {
            for (Router router : routerMap.values()) {
                router.onActivityPaused(activity);
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (this.activity == activity) {
            for (Router router : routerMap.values()) {
                router.onActivityStopped(activity);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (this.activity == activity) {
            for (Router router : routerMap.values()) {
                router.saveInstanceState(outState);
            }
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) { }
}
