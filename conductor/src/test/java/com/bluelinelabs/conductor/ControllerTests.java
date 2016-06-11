package com.bluelinelabs.conductor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Controller.RetainViewMode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ControllerTests {

    private ActivityController<TestActivity> mActivityController;
    private Router mRouter;

    public void createActivityController(Bundle savedInstanceState) {
        mActivityController = Robolectric.buildActivity(TestActivity.class).create(savedInstanceState).start();

        @IdRes int containerId = 4;
        FrameLayout routerContainer = new FrameLayout(mActivityController.get());
        routerContainer.setId(containerId);

        mRouter = Conductor.attachRouter(mActivityController.get(), routerContainer, savedInstanceState);
        if (!mRouter.hasRootController()) {
            mRouter.setRoot(RouterTransaction.builder(new TestController()).build());
        }
    }

    @Before
    public void setup() {
        createActivityController(null);
    }

    @Test
    public void testViewRetention() {
        Controller controller = new TestController();

        // Test View getting released w/ RELEASE_DETACH
        controller.setRetainViewMode(RetainViewMode.RELEASE_DETACH);
        Assert.assertNull(controller.getView());
        View view = controller.inflate(new FrameLayout(mRouter.getActivity()));
        Assert.assertNotNull(controller.getView());
        ViewUtils.setAttached(view, true);
        Assert.assertNotNull(controller.getView());
        ViewUtils.setAttached(view, false);
        Assert.assertNull(controller.getView());

        // Test View getting retained w/ RETAIN_DETACH
        controller.setRetainViewMode(RetainViewMode.RETAIN_DETACH);
        view = controller.inflate(new FrameLayout(mRouter.getActivity()));
        Assert.assertNotNull(controller.getView());
        ViewUtils.setAttached(view, true);
        Assert.assertNotNull(controller.getView());
        ViewUtils.setAttached(view, false);
        Assert.assertNotNull(controller.getView());

        // Ensure re-setting RELEASE_DETACH releases
        controller.setRetainViewMode(RetainViewMode.RELEASE_DETACH);
        Assert.assertNull(controller.getView());
    }

    @Test
    public void testActivityResult() {
        TestController controller = new TestController();
        CallState expectedCallState = new CallState(true);

        mRouter.pushController(RouterTransaction.builder(controller).build());
        ViewUtils.setAttached(controller.getView(), true);

        // Ensure that calling onActivityResult w/o requesting a result doesn't do anything
        mRouter.onActivityResult(1, Activity.RESULT_OK, null);
        assertCalls(expectedCallState, controller);

        // Ensure starting an activity for result gets us the result back
        controller.startActivityForResult(new Intent("action"), 1);
        mRouter.onActivityResult(1, Activity.RESULT_OK, null);
        expectedCallState.onActivityResultCalls++;
        assertCalls(expectedCallState, controller);

        // Ensure requesting a result w/o calling startActivityForResult works
        controller.registerForActivityResult(2);
        mRouter.onActivityResult(2, Activity.RESULT_OK, null);
        expectedCallState.onActivityResultCalls++;
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testActivityResultForChild() {
        TestController parent = new TestController();
        TestController child = new TestController();

        mRouter.pushController(RouterTransaction.builder(parent).build());
        ViewUtils.setAttached(parent.getView(), true);
        parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID), null)
                .setRoot(RouterTransaction.builder(child).build());
        ViewUtils.setAttached(child.getView(), true);

        CallState childExpectedCallState = new CallState(true);
        CallState parentExpectedCallState = new CallState(true);

        // Ensure that calling onActivityResult w/o requesting a result doesn't do anything
        mRouter.onActivityResult(1, Activity.RESULT_OK, null);
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure starting an activity for result gets us the result back
        child.startActivityForResult(new Intent("action"), 1);
        mRouter.onActivityResult(1, Activity.RESULT_OK, null);
        childExpectedCallState.onActivityResultCalls++;
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure requesting a result w/o calling startActivityForResult works
        child.registerForActivityResult(2);
        mRouter.onActivityResult(2, Activity.RESULT_OK, null);
        childExpectedCallState.onActivityResultCalls++;
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);
    }

    @Test
    public void testPermissionResult() {
        final String[] requestedPermissions = new String[] {"test"};

        TestController controller = new TestController();
        CallState expectedCallState = new CallState(true);

        mRouter.pushController(RouterTransaction.builder(controller).build());
        ViewUtils.setAttached(controller.getView(), true);

        // Ensure that calling handleRequestedPermission w/o requesting a result doesn't do anything
        mRouter.onRequestPermissionsResult("anotherId", 1, requestedPermissions, new int[] {1});
        assertCalls(expectedCallState, controller);

        // Ensure requesting the permission gets us the result back
        try {
            controller.requestPermissions(requestedPermissions, 1);
        } catch (NoSuchMethodError ignored) { }

        mRouter.onRequestPermissionsResult(controller.getInstanceId(), 1, requestedPermissions, new int[] {1});
        expectedCallState.onRequestPermissionsResultCalls++;
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testPermissionResultForChild() {
        final String[] requestedPermissions = new String[] {"test"};

        TestController parent = new TestController();
        TestController child = new TestController();

        mRouter.pushController(RouterTransaction.builder(parent).build());
        ViewUtils.setAttached(parent.getView(), true);
        parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID), null)
                .setRoot(RouterTransaction.builder(child).build());
        ViewUtils.setAttached(child.getView(), true);

        CallState childExpectedCallState = new CallState(true);
        CallState parentExpectedCallState = new CallState(true);

        // Ensure that calling handleRequestedPermission w/o requesting a result doesn't do anything
        mRouter.onRequestPermissionsResult("anotherId", 1, requestedPermissions, new int[] {1});
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure requesting the permission gets us the result back
        try {
            child.requestPermissions(requestedPermissions, 1);
        } catch (NoSuchMethodError ignored) { }

        mRouter.onRequestPermissionsResult(child.getInstanceId(), 1, requestedPermissions, new int[] {1});
        childExpectedCallState.onRequestPermissionsResultCalls++;
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);
    }

    @Test
    public void testOptionsMenu() {
        TestController controller = new TestController();
        CallState expectedCallState = new CallState(true);

        mRouter.pushController(RouterTransaction.builder(controller).build());
        ViewUtils.setAttached(controller.getView(), true);

        // Ensure that calling onCreateOptionsMenu w/o declaring that we have one doesn't do anything
        mRouter.onCreateOptionsMenu(null, null);
        assertCalls(expectedCallState, controller);

        // Ensure calling onCreateOptionsMenu with a menu works
        controller.setHasOptionsMenu(true);

        // Ensure it'll still get called back next time onCreateOptionsMenu is called
        mRouter.onCreateOptionsMenu(null, null);
        expectedCallState.createOptionsMenuCalls++;
        assertCalls(expectedCallState, controller);

        // Ensure we stop getting them when we hide it
        controller.setOptionsMenuHidden(true);
        mRouter.onCreateOptionsMenu(null, null);
        assertCalls(expectedCallState, controller);

        // Ensure we get the callback them when we un-hide it
        controller.setOptionsMenuHidden(false);
        mRouter.onCreateOptionsMenu(null, null);
        expectedCallState.createOptionsMenuCalls++;
        assertCalls(expectedCallState, controller);

        // Ensure we don't get the callback when we no longer have a menu
        controller.setHasOptionsMenu(false);
        mRouter.onCreateOptionsMenu(null, null);
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testOptionsMenuForChild() {
        TestController parent = new TestController();
        TestController child = new TestController();

        mRouter.pushController(RouterTransaction.builder(parent).build());
        ViewUtils.setAttached(parent.getView(), true);
        parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID), null)
                .setRoot(RouterTransaction.builder(child).build());
        ViewUtils.setAttached(child.getView(), true);

        CallState childExpectedCallState = new CallState(true);
        CallState parentExpectedCallState = new CallState(true);

        // Ensure that calling onCreateOptionsMenu w/o declaring that we have one doesn't do anything
        mRouter.onCreateOptionsMenu(null, null);
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure calling onCreateOptionsMenu with a menu works
        child.setHasOptionsMenu(true);

        // Ensure it'll still get called back next time onCreateOptionsMenu is called
        mRouter.onCreateOptionsMenu(null, null);
        childExpectedCallState.createOptionsMenuCalls++;
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure we stop getting them when we hide it
        child.setOptionsMenuHidden(true);
        mRouter.onCreateOptionsMenu(null, null);
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure we get the callback them when we un-hide it
        child.setOptionsMenuHidden(false);
        mRouter.onCreateOptionsMenu(null, null);
        childExpectedCallState.createOptionsMenuCalls++;
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);

        // Ensure we don't get the callback when we no longer have a menu
        child.setHasOptionsMenu(false);
        mRouter.onCreateOptionsMenu(null, null);
        assertCalls(childExpectedCallState, child);
        assertCalls(parentExpectedCallState, parent);
    }

    @Test
    public void testAddRemoveChildControllers() {
        TestController parent = new TestController();
        TestController child1 = new TestController();
        TestController child2 = new TestController();

        mRouter.pushController(RouterTransaction.builder(parent).build());

        Assert.assertEquals(0, parent.getChildRouters().size());
        Assert.assertNull(child1.getParentController());
        Assert.assertNull(child2.getParentController());

        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID), null);
        childRouter.setRoot(RouterTransaction.builder(child1).build());

        Assert.assertEquals(1, parent.getChildRouters().size());
        Assert.assertEquals(childRouter, parent.getChildRouters().get(0));
        Assert.assertEquals(1, childRouter.getBackstackSize());
        Assert.assertEquals(child1, childRouter.getControllers().get(0));
        Assert.assertEquals(parent, child1.getParentController());
        Assert.assertNull(child2.getParentController());

        childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID), null);
        childRouter.pushController(RouterTransaction.builder(child2).build());

        Assert.assertEquals(1, parent.getChildRouters().size());
        Assert.assertEquals(childRouter, parent.getChildRouters().get(0));
        Assert.assertEquals(2, childRouter.getBackstackSize());
        Assert.assertEquals(child1, childRouter.getControllers().get(0));
        Assert.assertEquals(child2, childRouter.getControllers().get(1));
        Assert.assertEquals(parent, child1.getParentController());
        Assert.assertEquals(parent, child2.getParentController());

        childRouter.popController(child2);

        Assert.assertEquals(1, parent.getChildRouters().size());
        Assert.assertEquals(childRouter, parent.getChildRouters().get(0));
        Assert.assertEquals(1, childRouter.getBackstackSize());
        Assert.assertEquals(child1, childRouter.getControllers().get(0));
        Assert.assertEquals(parent, child1.getParentController());
        Assert.assertNull(child2.getParentController());

        childRouter.popController(child1);

        Assert.assertEquals(1, parent.getChildRouters().size());
        Assert.assertEquals(childRouter, parent.getChildRouters().get(0));
        Assert.assertEquals(0, childRouter.getBackstackSize());
        Assert.assertNull(child1.getParentController());
        Assert.assertNull(child2.getParentController());
    }

    @Test
    public void testAddRemoveChildRouters() {
        TestController parent = new TestController();

        TestController child1 = new TestController();
        TestController child2 = new TestController();

        mRouter.pushController(RouterTransaction.builder(parent).build());

        Assert.assertEquals(0, parent.getChildRouters().size());
        Assert.assertNull(child1.getParentController());
        Assert.assertNull(child2.getParentController());

        Router childRouter1 = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.CHILD_VIEW_ID_1), null);
        Router childRouter2 = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.CHILD_VIEW_ID_2), null);

        childRouter1.setRoot(RouterTransaction.builder(child1).build());
        childRouter2.setRoot(RouterTransaction.builder(child2).build());

        Assert.assertEquals(2, parent.getChildRouters().size());
        Assert.assertEquals(childRouter1, parent.getChildRouters().get(0));
        Assert.assertEquals(childRouter2, parent.getChildRouters().get(1));
        Assert.assertEquals(1, childRouter1.getBackstackSize());
        Assert.assertEquals(1, childRouter2.getBackstackSize());
        Assert.assertEquals(child1, childRouter1.getControllers().get(0));
        Assert.assertEquals(child2, childRouter2.getControllers().get(0));
        Assert.assertEquals(parent, child1.getParentController());
        Assert.assertEquals(parent, child2.getParentController());

        parent.removeChildRouter(childRouter2);

        Assert.assertEquals(1, parent.getChildRouters().size());
        Assert.assertEquals(childRouter1, parent.getChildRouters().get(0));
        Assert.assertEquals(1, childRouter1.getBackstackSize());
        Assert.assertEquals(0, childRouter2.getBackstackSize());
        Assert.assertEquals(child1, childRouter1.getControllers().get(0));
        Assert.assertEquals(parent, child1.getParentController());
        Assert.assertNull(child2.getParentController());

        parent.removeChildRouter(childRouter1);

        Assert.assertEquals(0, parent.getChildRouters().size());
        Assert.assertEquals(0, childRouter1.getBackstackSize());
        Assert.assertEquals(0, childRouter2.getBackstackSize());
        Assert.assertNull(child1.getParentController());
        Assert.assertNull(child2.getParentController());
    }

    private void assertCalls(CallState callState, TestController controller) {
        Assert.assertEquals("Expected call counts and controller call counts do not match.", callState, controller.currentCallState);
    }

}
