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

    override fun isSectionIdCanceled(sectionId: Long)
            = canceledSectionIdsSet.contains(sectionId)
}
