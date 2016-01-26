package org.stepic.droid.core;

public interface ILocalProgressManager {
    /**
     * may be high-weight operation
     *
     * @param stepId of unit
     */
    void checkUnitAsPassed(long stepId);
}