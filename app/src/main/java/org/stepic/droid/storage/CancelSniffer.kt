package org.stepic.droid.storage

interface CancelSniffer {
    fun addStepIdCancel(stepId: Long)

    fun removeStepIdCancel(stepId: Long)

    fun isStepIdCanceled(stepId: Long): Boolean

    fun addSectionIdCancel(sectionId: Long)

    fun removeSectionIdCancel(sectionId: Long)

    fun isSectionIdCanceled(sectionId: Long): Boolean
}
