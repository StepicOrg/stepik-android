package org.stepic.droid.core;

import android.support.annotation.WorkerThread;

public interface LocalProgressManager {
    /**
     * may be high-weight operation
     *
     * @param stepId of unit
     */
    @WorkerThread
    void checkUnitAsPassed(long stepId);

    @WorkerThread
    void updateUnitProgress(long unitId);
}