package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.Course
import org.stepik.android.model.Section

interface ContinueCourseView {

    fun onShowContinueCourseLoadingDialog()

    fun onOpenStep(courseId: Long, section: Section, lessonId: Long, unitId: Long, stepPosition: Int)

    fun onOpenAdaptiveCourse(course: Course)

    fun onAnyProblemWhileContinue(course: Course)
}