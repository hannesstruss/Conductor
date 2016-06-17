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

    private Router mRouter;

    @Before
    public void setup() {
        Activity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        mRouter = Conductor.attachRouter(activity, new FrameLayout(activity), null);
    }

    @Test
    public void testSetRoot() {
        String rootTag = "root";

        Controller rootController = new TestController();

        Assert.assertFalse(mRouter.hasRootController());

        mRouter.setRoot(RouterTransaction.with(rootController).tag(rootTag));

        Assert.assertTrue(mRouter.hasRootController());

        Assert.assertEquals(rootController, mRouter.getControllerWithTag(rootTag));
    }

    @Test
    public void testSetNewRoot() {
        String oldRootTag = "oldRoot";
        String newRootTag = "newRoot";

        Controller oldRootController = new TestController();
        Controller newRootController = new TestController();

        mRouter.setRoot(RouterTransaction.with(oldRootController).tag(oldRootTag));
        mRouter.setRoot(RouterTransaction.with(newRootController).tag(newRootTag));

        Assert.assertNull(mRouter.getControllerWithTag(oldRootTag));
        Assert.assertEquals(newRootController, mRouter.getControllerWithTag(newRootTag));
    }

    @Test
    public void testGetByInstanceId() {
        Controller controller = new TestController();

        mRouter.pushController(RouterTransaction.with(controller));

        Assert.assertEquals(controller, mRouter.getControllerWithInstanceId(controller.getInstanceId()));
        Assert.assertNull(mRouter.getControllerWithInstanceId("fake id"));
    }

    @Test
    public void testGetByTag() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        mRouter.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        mRouter.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        Assert.assertEquals(controller1, mRouter.getControllerWithTag(controller1Tag));
        Assert.assertEquals(controller2, mRouter.getControllerWithTag(controller2Tag));
    }

    @Test
    public void testPushPopControllers() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();

        mRouter.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        Assert.assertEquals(1, mRouter.getBackstackSize());

        mRouter.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        Assert.assertEquals(2, mRouter.getBackstackSize());

        mRouter.popCurrentController();

        Assert.assertEquals(1, mRouter.getBackstackSize());

        Assert.assertEquals(controller1, mRouter.getControllerWithTag(controller1Tag));
        Assert.assertNull(mRouter.getControllerWithTag(controller2Tag));

        mRouter.popCurrentController();

        Assert.assertEquals(0, mRouter.getBackstackSize());

        Assert.assertNull(mRouter.getControllerWithTag(controller1Tag));
        Assert.assertNull(mRouter.getControllerWithTag(controller2Tag));
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

        mRouter.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        mRouter.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        mRouter.pushController(RouterTransaction.with(controller3)
                .tag(controller3Tag));

        mRouter.pushController(RouterTransaction.with(controller4)
                .tag(controller4Tag));

        mRouter.popToTag(controller2Tag);

        Assert.assertEquals(2, mRouter.getBackstackSize());
        Assert.assertEquals(controller1, mRouter.getControllerWithTag(controller1Tag));
        Assert.assertEquals(controller2, mRouter.getControllerWithTag(controller2Tag));
        Assert.assertNull(mRouter.getControllerWithTag(controller3Tag));
        Assert.assertNull(mRouter.getControllerWithTag(controller4Tag));
    }

    @Test
    public void testPopNonCurrent() {
        String controller1Tag = "controller1";
        String controller2Tag = "controller2";
        String controller3Tag = "controller3";

        Controller controller1 = new TestController();
        Controller controller2 = new TestController();
        Controller controller3 = new TestController();

        mRouter.pushController(RouterTransaction.with(controller1)
                .tag(controller1Tag));

        mRouter.pushController(RouterTransaction.with(controller2)
                .tag(controller2Tag));

        mRouter.pushController(RouterTransaction.with(controller3)
                .tag(controller3Tag));

        mRouter.popController(controller2);

        Assert.assertEquals(2, mRouter.getBackstackSize());
        Assert.assertEquals(controller1, mRouter.getControllerWithTag(controller1Tag));
        Assert.assertNull(mRouter.getControllerWithTag(controller2Tag));
        Assert.assertEquals(controller3, mRouter.getControllerWithTag(controller3Tag));
    }

}
