package org.stepic.droid.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RWLocks {
    public static final ReentrantReadWriteLock DatabaseLock = new ReentrantReadWriteLock();
    public static final ReentrantReadWriteLock ClearEnrollmentsLock = new ReentrantReadWriteLock();
}
