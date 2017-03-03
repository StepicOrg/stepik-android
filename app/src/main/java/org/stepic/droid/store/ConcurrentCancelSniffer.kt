package org.stepic.droid.store

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ConcurrentCancelSniffer : CancelSniffer {

    private val canceledStepIdsSet: MutableSet<Long> = Collections.newSetFromMap(ConcurrentHashMap<Long, Boolean>())
    private val canceledSectionIdsSet: MutableSet<Long> = Collections.newSetFromMap(ConcurrentHashMap<Long, Boolean>())
    private val canceledUnitIdsSet: MutableSet<Long> = Collections.newSetFromMap(ConcurrentHashMap<Long, Boolean>())

    override fun addStepIdCancel(stepId: Long) {
        canceledStepIdsSet.add(stepId)
    }

    override fun removeStepIdCancel(stepId: Long) {
        canceledStepIdsSet.remove(stepId)
    }

    override fun isStepIdCanceled(stepId: Long)
            = canceledStepIdsSet.contains(stepId)

    override fun addSectionIdCancel(sectionId: Long) {
        canceledSectionIdsSet.add(sectionId)
    }

    override fun removeSectionIdCancel(sectionId: Long) {
        canceledSectionIdsSet.remove(sectionId)
    }

    override fun isSectionIdIsCanceled(sectionId: Long)
            = canceledSectionIdsSet.contains(sectionId)

    override fun addLessonToCancel(lessonId: Long) {
        canceledUnitIdsSet.add(lessonId)
    }

    override fun removeLessonIdToCancel(lessonId: Long) {
        canceledUnitIdsSet.remove(lessonId)
    }

    override fun isLessonIdIsCanceled(lessonId: Long)
            = canceledUnitIdsSet.contains(lessonId)
}
