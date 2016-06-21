package com.bluelinelabs.conductor;

import android.os.Bundle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

class Backstack implements Iterable<RouterTransaction> {

    private static final String KEY_ENTRIES = "Backstack.entries";

    private final Deque<RouterTransaction> backStack = new ArrayDeque<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEmpty() {
        return backStack.isEmpty();
    }

    public int size() {
        return backStack.size();
    }

    public RouterTransaction root() {
        return backStack.size() > 0 ? backStack.getLast() : null;
    }

    @Override
    public Iterator<RouterTransaction> iterator() {
        return backStack.iterator();
    }

    public Iterator<RouterTransaction> reverseIterator() {
        return backStack.descendingIterator();
    }

    public List<RouterTransaction> popTo(RouterTransaction transaction) {
        List<RouterTransaction> popped = new ArrayList<>();
        if (backStack.contains(transaction)) {
            while (backStack.peek() != transaction) {
                RouterTransaction poppedTransaction = pop();
                popped.add(poppedTransaction);
            }
        } else {
            throw new RuntimeException("Tried to pop to a transaction that was not on the back stack");
        }
        return popped;
    }

    public RouterTransaction pop() {
        RouterTransaction popped = backStack.pop();
        popped.controller.destroy();
        return popped;
    }

    public RouterTransaction peek() {
        return backStack.peek();
    }

    public void remove(RouterTransaction transaction) {
        backStack.removeFirstOccurrence(transaction);
    }

    public void push(RouterTransaction transaction) {
        backStack.push(transaction);
    }

    public List<RouterTransaction> popAll() {
        List<RouterTransaction> list = new ArrayList<>();
        while (!isEmpty()) {
            list.add(pop());
        }
        return list;
    }

    public void setBackstack(List<RouterTransaction> backstack) {
        for (RouterTransaction existingTransaction : backStack) {
            boolean contains = false;
            for (RouterTransaction newTransaction : backstack) {
                if (existingTransaction.controller == newTransaction.controller) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                existingTransaction.controller.destroy();
            }
        }

        backStack.clear();
        for (RouterTransaction transaction : backstack) {
            backStack.push(transaction);
        }
    }

    public void saveInstanceState(Bundle outState) {
        ArrayList<Bundle> entryBundles = new ArrayList<>(backStack.size());
        for (RouterTransaction entry : backStack) {
            entryBundles.add(entry.saveInstanceState());
        }

        outState.putParcelableArrayList(KEY_ENTRIES, entryBundles);
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        ArrayList<Bundle> entryBundles = savedInstanceState.getParcelableArrayList(KEY_ENTRIES);
        if (entryBundles != null) {
            Collections.reverse(entryBundles);
            for (Bundle transactionBundle : entryBundles) {
                backStack.push(new RouterTransaction(transactionBundle));
            }
        }
    }
}
