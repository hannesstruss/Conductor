package com.bluelinelabs.conductor;

import android.app.Activity;
import android.widget.FrameLayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RouterTests {

    private Router router;

    @Before
    public void setup() {
        Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        router = Conductor.attachRouter(activity, new FrameLayout(activity), null);
    }

    @Test
    public void testSetRoot() {
        String rootTag = "root";

        Controller rootController = new TestController();

        Assert.assertFalse(router.hasRootController());

        router.setRoot(RouterTransaction.with(rootController).tag(rootTag));

        Assert.assertTrue(router.hasRootController());

        Assert.assertEquals(rootController, router.getControllerWithTag(rootTag));
    }

    @Test
    public void testSetNewRoot() {
        String oldRootTag = "oldRoot";
        String newRootTag = "newRoot";

        Controller oldRootController = new TestController();
        Controller newRootController = new TestController();

        router.setRoot(RouterTransaction.with(oldRootController).tag(oldRootTag));
        router.setRoot(RouterTransaction.with(newRootController).tag(newRootTag));

        Assert.assertNull(router.getControllerWithTag(oldRootTag));
        Assert.assertEquals(newRootController, router.getControllerWithTag(newRootTag));
    }

    @Test
    public void testGetByInstanceId() {
        Controller controller = new TestController();

        router.pushController(RouterTransaction.with(controller));

        Assert.assertEquals(controller, router.getControllerWithInstanceId(controller.getInstanceId()));
        Assert.assertNull(router.getControllerWithInstanceId("fake id"));
    }

    @Test
    public void testGetByTag() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        Assert.assertEquals(controller2, router.getControllerWithTag(controller2Tag));
    }

    @Test
    public void testPushPopControllers() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        Assert.assertEquals(1, router.getBackstackSize());

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        Assert.assertEquals(2, router.getBackstackSize());

        router.popCurrentController();

        Assert.assertEquals(1, router.getBackstackSize());

        Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        Assert.assertNull(router.getControllerWithTag(controller2Tag));

        router.popCurrentController();

        Assert.assertEquals(0, router.getBackstackSize());

        Assert.assertNull(router.getControllerWithTag(controller1Tag));
        Assert.assertNull(router.getControllerWithTag(controller2Tag));
    }

    @Test
    public void testPopToTag() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";
        String controller3Tag = "controller3";
        String controller4Tag = "controller4";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();
        Controller controller3 = new TestController();
        Controller controller4 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        router.pushController(RouterTransaction.with(controller3)
                .tag(controller3Tag));

        router.pushController(RouterTransaction.with(controller4)
                .tag(controller4Tag));

        router.popToTag(controller2Tag);

        Assert.assertEquals(2, router.getBackstackSize());
        Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        Assert.assertEquals(controller2, router.getControllerWithTag(controller2Tag));
        Assert.assertNull(router.getControllerWithTag(controller3Tag));
        Assert.assertNull(router.getControllerWithTag(controller4Tag));
    }

    @Test
    public void testPopNonCurrent() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";
        String controller3Tag = "controller3";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();
        Controller controller3 = new TestController();

        router.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        router.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        router.pushController(RouterTransaction.with(controller3)
                .tag(controller3Tag));

        router.popController(controller2);

        Assert.assertEquals(2, router.getBackstackSize());
        Assert.assertEquals(controller1, router.getControllerWithTag(controller1Tag));
        Assert.assertNull(router.getControllerWithTag(controller2Tag));
        Assert.assertEquals(controller3, router.getControllerWithTag(controller3Tag));
    }

}
