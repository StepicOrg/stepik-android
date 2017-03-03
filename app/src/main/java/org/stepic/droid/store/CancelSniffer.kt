package org.stepic.droid.store

interface CancelSniffer {

    fun addStepIdCancel(stepId: Long)

    fun removeStepIdCancel(stepId: Long)

    fun isStepIdCanceled(stepId: Long): Boolean

    fun addSectionIdCancel(sectionId: Long)

    fun removeSectionIdCancel(sectionId: Long)

    fun isSectionIdIsCanceled(sectionId: Long): Boolean

    fun addLessonToCancel(lessonId: Long)

    fun removeLessonIdToCancel(lessonId: Long)

    fun isLessonIdIsCanceled(lessonId: Long): Boolean
}
