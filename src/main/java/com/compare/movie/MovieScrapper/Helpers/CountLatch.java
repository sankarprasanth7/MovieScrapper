package com.compare.movie.MovieScrapper.Helpers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class CountLatch {

@SuppressWarnings("serial")
private static final class Sync extends AbstractQueuedSynchronizer {

    Sync(int count) {
        setState(count);
    }

    int getCount() {
        return getState();
    }

    protected int tryAcquireShared(int acquires) {
        return (getState() == 0) ? 1 : -1;
    }

    protected int acquireNonBlocking(int acquires) {
        // increment count
        for (;;) {
            int c = getState();
            int nextc = c + 1;
            if (compareAndSetState(c, nextc))
                return 1;
        }
    }

    protected boolean tryReleaseShared(int releases) {
        // Decrement count; signal when transition to zero
        for (;;) {
            int c = getState();
            if (c == 0)
                return false;
            int nextc = c - 1;
            if (compareAndSetState(c, nextc))
                return nextc == 0;
        }
    }
}

private final Sync sync;

public CountLatch(int count) {
    this.sync = new Sync(count);
}

public void awaitZero() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}

public boolean awaitZero(long timeout, TimeUnit unit) throws InterruptedException {
    return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
}

public void increment() {
    sync.acquireNonBlocking(1);
}

public void decrement() {
    sync.releaseShared(1);
}

public int toInt() {
    return   sync.getCount();
}

}