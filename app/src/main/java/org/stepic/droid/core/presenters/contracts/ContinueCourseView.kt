package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.structure.Course
import org.stepik.android.model.structure.Section

interface ContinueCourseView {

    fun onShowContinueCourseLoadingDialog()

    fun onOpenStep(courseId: Long, section: Section, lessonId: Long, unitId: Long, stepPosition: Int)

    fun onOpenAdaptiveCourse(course: Course)

    fun onAnyProblemWhileContinue(course: Course)
}