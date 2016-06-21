package com.bluelinelabs.conductor;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Controller.LifecycleListener;
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeCompletedListener;

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
public class ControllerLifecycleTests {

    private ActivityController<TestActivity> activityController;
    private Router router;

    private CallState currentCallState;

    public void createActivityController(Bundle savedInstanceState) {
        activityController = Robolectric.buildActivity(TestActivity.class).create(savedInstanceState).start();

        @IdRes int containerId = 4;
        FrameLayout routerContainer = new FrameLayout(activityController.get());
        routerContainer.setId(containerId);

        router = Conductor.attachRouter(activityController.get(), routerContainer, savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new TestController()));
        }
    }

    @Before
    public void setup() {
        createActivityController(null);

        currentCallState = new CallState();
    }

    @Test
    public void testNormalLifecycle() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState();

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller))
                .popChangeHandler(getPopHandler(expectedCallState, controller)));

        assertCalls(expectedCallState, controller);

        router.popCurrentController();

        Assert.assertNull(controller.getView());

        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testLifecycleWithActivityDestroy() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState();

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller)));

        assertCalls(expectedCallState, controller);

        activityController.pause();

        assertCalls(expectedCallState, controller);

        activityController.stop();

        assertCalls(expectedCallState, controller);

        activityController.destroy();

        expectedCallState.detachCalls++;
        expectedCallState.destroyViewCalls++;
        expectedCallState.destroyCalls++;
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testLifecycleWithActivityConfigurationChange() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState();

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller))
                .tag("root"));

        assertCalls(expectedCallState, controller);

        activityController.get().isChangingConfigurations = true;

        Bundle bundle = new Bundle();
        activityController.saveInstanceState(bundle);

        expectedCallState.saveViewStateCalls++;
        expectedCallState.saveInstanceStateCalls++;
        assertCalls(expectedCallState, controller);

        activityController.pause();
        assertCalls(expectedCallState, controller);

        activityController.stop();
        assertCalls(expectedCallState, controller);

        activityController.destroy();
        expectedCallState.detachCalls++;
        expectedCallState.destroyViewCalls++;
        assertCalls(expectedCallState, controller);

        createActivityController(bundle);
        controller = (TestController)router.getControllerWithTag("root");

        expectedCallState.restoreInstanceStateCalls++;
        expectedCallState.restoreViewStateCalls++;
        expectedCallState.changeStartCalls++;
        expectedCallState.changeEndCalls++;
        expectedCallState.createViewCalls++;

        // Lifecycle listener isn't attached during restore, grab the current views from the controller for this stuff...
        currentCallState.restoreInstanceStateCalls = controller.currentCallState.restoreInstanceStateCalls;
        currentCallState.restoreViewStateCalls = controller.currentCallState.restoreViewStateCalls;
        currentCallState.changeStartCalls = controller.currentCallState.changeStartCalls;
        currentCallState.changeEndCalls = controller.currentCallState.changeEndCalls;
        currentCallState.createViewCalls = controller.currentCallState.createViewCalls;

        assertCalls(expectedCallState, controller);

        activityController.resume();
        assertCalls(expectedCallState, controller);
    }

    @Test
    public void testLifecycleWithActivityBackground() {
        TestController controller = new TestController();
        attachLifecycleListener(controller);

        CallState expectedCallState = new CallState();

        assertCalls(expectedCallState, controller);
        router.pushController(RouterTransaction.with(controller)
                .pushChangeHandler(getPushHandler(expectedCallState, controller)));

        assertCalls(expectedCallState, controller);

        activityController.pause();

        Bundle bundle = new Bundle();
        activityController.saveInstanceState(bundle);

        expectedCallState.saveInstanceStateCalls++;
        expectedCallState.saveViewStateCalls++;
        assertCalls(expectedCallState, controller);

        activityController.resume();
    }

    @Test
    public void testLifecycleCallOrder() {
        final TestController testController = new TestController();
        final CallState callState = new CallState();

        testController.addLifecycleListener(new LifecycleListener() {
            @Override
            public void preCreateView(@NonNull Controller controller) {
                callState.createViewCalls++;
                Assert.assertEquals(1, callState.createViewCalls);
                Assert.assertEquals(0, testController.currentCallState.createViewCalls);

                Assert.assertEquals(0, callState.attachCalls);
                Assert.assertEquals(0, testController.currentCallState.attachCalls);

                Assert.assertEquals(0, callState.detachCalls);
                Assert.assertEquals(0, testController.currentCallState.detachCalls);

                Assert.assertEquals(0, callState.destroyViewCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(0, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postCreateView(@NonNull Controller controller, @NonNull View view) {
                callState.createViewCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(0, callState.attachCalls);
                Assert.assertEquals(0, testController.currentCallState.attachCalls);

                Assert.assertEquals(0, callState.detachCalls);
                Assert.assertEquals(0, testController.currentCallState.detachCalls);

                Assert.assertEquals(0, callState.destroyViewCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(0, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void preAttach(@NonNull Controller controller, @NonNull View view) {
                callState.attachCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(1, callState.attachCalls);
                Assert.assertEquals(0, testController.currentCallState.attachCalls);

                Assert.assertEquals(0, callState.detachCalls);
                Assert.assertEquals(0, testController.currentCallState.detachCalls);

                Assert.assertEquals(0, callState.destroyViewCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(0, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postAttach(@NonNull Controller controller, @NonNull View view) {
                callState.attachCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(2, callState.attachCalls);
                Assert.assertEquals(1, testController.currentCallState.attachCalls);

                Assert.assertEquals(0, callState.detachCalls);
                Assert.assertEquals(0, testController.currentCallState.detachCalls);

                Assert.assertEquals(0, callState.destroyViewCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(0, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void preDetach(@NonNull Controller controller, @NonNull View view) {
                callState.detachCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(2, callState.attachCalls);
                Assert.assertEquals(1, testController.currentCallState.attachCalls);

                Assert.assertEquals(1, callState.detachCalls);
                Assert.assertEquals(0, testController.currentCallState.detachCalls);

                Assert.assertEquals(0, callState.destroyViewCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(0, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postDetach(@NonNull Controller controller, @NonNull View view) {
                callState.detachCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(2, callState.attachCalls);
                Assert.assertEquals(1, testController.currentCallState.attachCalls);

                Assert.assertEquals(2, callState.detachCalls);
                Assert.assertEquals(1, testController.currentCallState.detachCalls);

                Assert.assertEquals(0, callState.destroyViewCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(0, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
                callState.destroyViewCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(2, callState.attachCalls);
                Assert.assertEquals(1, testController.currentCallState.attachCalls);

                Assert.assertEquals(2, callState.detachCalls);
                Assert.assertEquals(1, testController.currentCallState.detachCalls);

                Assert.assertEquals(1, callState.destroyViewCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(0, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postDestroyView(@NonNull Controller controller) {
                callState.destroyViewCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(2, callState.attachCalls);
                Assert.assertEquals(1, testController.currentCallState.attachCalls);

                Assert.assertEquals(2, callState.detachCalls);
                Assert.assertEquals(1, testController.currentCallState.detachCalls);

                Assert.assertEquals(2, callState.destroyViewCalls);
                Assert.assertEquals(1, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(0, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void preDestroy(@NonNull Controller controller) {
                callState.destroyCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(2, callState.attachCalls);
                Assert.assertEquals(1, testController.currentCallState.attachCalls);

                Assert.assertEquals(2, callState.detachCalls);
                Assert.assertEquals(1, testController.currentCallState.detachCalls);

                Assert.assertEquals(2, callState.destroyViewCalls);
                Assert.assertEquals(1, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(1, callState.destroyCalls);
                Assert.assertEquals(0, testController.currentCallState.destroyCalls);
            }

            @Override
            public void postDestroy(@NonNull Controller controller) {
                callState.destroyCalls++;
                Assert.assertEquals(2, callState.createViewCalls);
                Assert.assertEquals(1, testController.currentCallState.createViewCalls);

                Assert.assertEquals(2, callState.attachCalls);
                Assert.assertEquals(1, testController.currentCallState.attachCalls);

                Assert.assertEquals(2, callState.detachCalls);
                Assert.assertEquals(1, testController.currentCallState.detachCalls);

                Assert.assertEquals(2, callState.destroyViewCalls);
                Assert.assertEquals(1, testController.currentCallState.destroyViewCalls);

                Assert.assertEquals(2, callState.destroyCalls);
                Assert.assertEquals(1, testController.currentCallState.destroyCalls);
            }
        });

        router.pushController(RouterTransaction.with(testController)
                .pushChangeHandler(new ChangeHandler(new ChangeHandlerListener() {
                    @Override
                    public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
                        container.addView(to);
                        ViewUtils.setAttached(to, true);
                        changeListener.onChangeCompleted();
                    }
                }))
                .popChangeHandler(new ChangeHandler(new ChangeHandlerListener() {
                    @Override
                    public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
                        container.removeView(from);
                        ViewUtils.setAttached(from, false);
                        changeListener.onChangeCompleted();
                    }
                })));

        router.popController(testController);

        Assert.assertEquals(2, callState.createViewCalls);
        Assert.assertEquals(2, callState.attachCalls);
        Assert.assertEquals(2, callState.detachCalls);
        Assert.assertEquals(2, callState.destroyViewCalls);
        Assert.assertEquals(2, callState.destroyCalls);
    }

    @Test
    public void testChildLifecycle() {
        Controller parent = new TestController();
        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(new ChangeHandler(new ChangeHandlerListener() {
                    @Override
                    public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
                        container.addView(to);
                        ViewUtils.setAttached(to, true);
                        changeListener.onChangeCompleted();
                    }
                })));

        TestController child = new TestController();
        attachLifecycleListener(child);

        CallState expectedCallState = new CallState();

        assertCalls(expectedCallState, child);

        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID), null);
            childRouter
                .setRoot(RouterTransaction.with(child)
                        .pushChangeHandler(getPushHandler(expectedCallState, child))
                        .popChangeHandler(getPopHandler(expectedCallState, child)));

        assertCalls(expectedCallState, child);

        parent.removeChildRouter(childRouter);

        assertCalls(expectedCallState, child);
    }

    @Test
    public void testChildLifecycle2() {
        Controller parent = new TestController();
        router.pushController(RouterTransaction.with(parent)
                .pushChangeHandler(new ChangeHandler(new ChangeHandlerListener() {
                    @Override
                    public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
                        container.addView(to);
                        ViewUtils.setAttached(to, true);
                        changeListener.onChangeCompleted();
                    }
                }))
                .popChangeHandler(new ChangeHandler(new ChangeHandlerListener() {
                    @Override
                    public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
                        container.removeView(from);
                        ViewUtils.setAttached(from, false);
                        changeListener.onChangeCompleted();
                    }
                })));

        TestController child = new TestController();
        attachLifecycleListener(child);

        CallState expectedCallState = new CallState();

        assertCalls(expectedCallState, child);

        Router childRouter = parent.getChildRouter((ViewGroup)parent.getView().findViewById(TestController.VIEW_ID), null);
        childRouter
                .setRoot(RouterTransaction.with(child)
                        .pushChangeHandler(getPushHandler(expectedCallState, child))
                        .popChangeHandler(getPopHandler(expectedCallState, child)));

        assertCalls(expectedCallState, child);

        router.popCurrentController();

        assertCalls(expectedCallState, child);
    }

    private ChangeHandler getPushHandler(final CallState expectedCallState, final TestController controller) {
        return new ChangeHandler(new ChangeHandlerListener() {
            @Override
            public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
                expectedCallState.changeStartCalls++;
                expectedCallState.createViewCalls++;
                assertCalls(expectedCallState, controller);

                container.addView(to);
                ViewUtils.setAttached(to, true);

                expectedCallState.attachCalls++;
                assertCalls(expectedCallState, controller);

                changeListener.onChangeCompleted();

                expectedCallState.changeEndCalls++;
                assertCalls(expectedCallState, controller);
            }
        });
    }

    private ChangeHandler getPopHandler(final CallState expectedCallState, final TestController controller) {
        return new ChangeHandler(new ChangeHandlerListener() {
            @Override
            public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
                expectedCallState.changeStartCalls++;
                assertCalls(expectedCallState, controller);
                container.removeView(from);
                ViewUtils.setAttached(from, false);

                expectedCallState.destroyViewCalls++;
                expectedCallState.detachCalls++;
                expectedCallState.destroyCalls++;
                assertCalls(expectedCallState, controller);

                changeListener.onChangeCompleted();

                expectedCallState.changeEndCalls++;
                assertCalls(expectedCallState, controller);
            }
        });
    }

    private void assertCalls(CallState callState, TestController controller) {
        Assert.assertEquals("Expected call counts and controller call counts do not match.", callState, controller.currentCallState);
        Assert.assertEquals("Expected call counts and lifecycle call counts do not match.", callState, currentCallState);
    }

    private void attachLifecycleListener(Controller controller) {
        controller.addLifecycleListener(new LifecycleListener() {
            @Override
            public void onChangeStart(@NonNull Controller controller, @NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
                currentCallState.changeStartCalls++;
            }

            @Override
            public void onChangeEnd(@NonNull Controller controller, @NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
                currentCallState.changeEndCalls++;
            }

            @Override
            public void postCreateView(@NonNull Controller controller, @NonNull View view) {
                currentCallState.createViewCalls++;
            }

            @Override
            public void postAttach(@NonNull Controller controller, @NonNull View view) {
                currentCallState.attachCalls++;
            }

            @Override
            public void postDestroyView(@NonNull Controller controller) {
                currentCallState.destroyViewCalls++;
            }

            @Override
            public void postDetach(@NonNull Controller controller, @NonNull View view) {
                currentCallState.detachCalls++;
            }

            @Override
            public void postDestroy(@NonNull Controller controller) {
                currentCallState.destroyCalls++;
            }

            @Override
            public void onSaveInstanceState(@NonNull Controller controller, @NonNull Bundle outState) {
                currentCallState.saveInstanceStateCalls++;
            }

            @Override
            public void onRestoreInstanceState(@NonNull Controller controller, @NonNull Bundle savedInstanceState) {
                currentCallState.restoreInstanceStateCalls++;
            }

            @Override
            public void onSaveViewState(@NonNull Controller controller, @NonNull Bundle outState) {
                currentCallState.saveViewStateCalls++;
            }

            @Override
            public void onRestoreViewState(@NonNull Controller controller, @NonNull Bundle savedViewState) {
                currentCallState.restoreViewStateCalls++;
            }
        });
    }

    interface ChangeHandlerListener {
        void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener);
    }

    public static class ChangeHandler extends ControllerChangeHandler {

        private ChangeHandlerListener listener;

        public ChangeHandler() { }

        public ChangeHandler(ChangeHandlerListener listener) {
            this.listener = listener;
        }

        @Override
        public void performChange(@NonNull ViewGroup container, View from, View to, boolean isPush, @NonNull ControllerChangeCompletedListener changeListener) {
            listener.performChange(container, from, to, isPush, changeListener);
        }
    }

}
