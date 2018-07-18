package org.stepic.droid.storage

import org.stepik.android.model.structure.Step

//todo: do interface segregation: for units and for sections
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


    interface SectionCallback {
        fun onSectionCached(sectionId: Long)

        fun onSectionNotCached(sectionId: Long)
    }

    fun addSectionCallback(callback: SectionCallback)

    fun removeSectionCallback(callback: SectionCallback)

}
