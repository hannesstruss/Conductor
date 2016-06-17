package com.bluelinelabs.conductor;

import android.os.Bundle;

import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;

import junit.framework.Assert;

import org.junit.Test;

public class ControllerTransactionTests {

    @Test
    public void testRouterSaveRestore() {
        RouterTransaction transaction = RouterTransaction.builder(new TestController())
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new VerticalChangeHandler())
                .tag("Test Tag")
                .build();

        Bundle bundle = transaction.saveInstanceState();

        RouterTransaction restoredTransaction = new RouterTransaction(bundle);

        Assert.assertEquals(transaction.controller().getClass(), restoredTransaction.controller().getClass());
        Assert.assertEquals(transaction.pushControllerChangeHandler().getClass(), restoredTransaction.pushControllerChangeHandler().getClass());
        Assert.assertEquals(transaction.popControllerChangeHandler().getClass(), restoredTransaction.popControllerChangeHandler().getClass());
        Assert.assertEquals(transaction.tag(), restoredTransaction.tag());
    }

}
