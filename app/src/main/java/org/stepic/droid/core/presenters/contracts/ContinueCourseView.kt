package org.stepic.droid.core.presenters.contracts

import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course

interface ContinueCourseView {

    fun onShowContinueCourseLoadingDialog()

    fun onOpenStep(courseId: Long, lastStep: LastStep)

    fun onOpenAdaptiveCourse(course: Course)

    fun onAnyProblemWhileContinue(course: Course)
}