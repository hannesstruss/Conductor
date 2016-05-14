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

        Assert.assertEquals(transaction.getController().getClass(), restoredTransaction.getController().getClass());
        Assert.assertEquals(transaction.getPushControllerChangeHandler().getClass(), restoredTransaction.getPushControllerChangeHandler().getClass());
        Assert.assertEquals(transaction.getPopControllerChangeHandler().getClass(), restoredTransaction.getPopControllerChangeHandler().getClass());
        Assert.assertEquals(transaction.getTag(), restoredTransaction.getTag());
    }

}
