package org.stepic.droid.storage

import org.stepic.droid.model.Step

interface StoreStateManager {

    fun updateUnitLessonState(lessonId: Long)

    fun updateUnitLessonAfterDeleting(lessonId: Long)

    fun updateStepAfterDeleting(step: Step?)

    fun updateSectionAfterDeleting(sectionId: Long)

    fun updateSectionState(sectionId: Long)


    interface LessonCallback {
        fun onLessonCached(lessonId: Long)

        fun onLessonNotCached(lessonId: Long)
    }

    fun addLessonCallback(callback: LessonCallback)

    fun removeLessonCallback(callback: LessonCallback)

}
