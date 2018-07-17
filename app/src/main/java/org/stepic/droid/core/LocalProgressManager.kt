package org.stepic.droid.core

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepik.android.model.structure.Progress

interface LocalProgressManager {
    /**
     * may be high-weight operation

     * @param stepId of unit
     */
    @WorkerThread
    fun checkUnitAsPassed(stepId: Long)

    @WorkerThread
    fun updateUnitProgress(unitId: Long)

    @MainThread
    fun subscribe(unitProgressListener: UnitProgressListener)

    @MainThread
    fun unsubscribe(unitProgressListener: UnitProgressListener)

    interface UnitProgressListener {

        fun onScoreUpdated(unitId: Long, newScore: Double)

        fun onUnitPassed(unitId: Long)
    }


    interface SectionProgressListener {
        fun onProgressUpdated(newProgress: Progress, courseId: Long)
    }
    @MainThread
    fun subscribe(sectionProgressListener: SectionProgressListener)

    @MainThread
    fun unsubscribe(sectionProgressListener: SectionProgressListener)

}