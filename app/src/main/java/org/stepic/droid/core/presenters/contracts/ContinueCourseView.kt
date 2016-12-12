package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Section

interface ContinueCourseView {

    fun onShowContinueCourseLoadingDialog()

    fun onOpenStep(courseId: Long, section: Section, lessonId: Long, unitId: Long, stepPosition: Int)

    fun onConnectionProblemWhileContinue()
}