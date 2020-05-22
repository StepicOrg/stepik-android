package org.stepik.android.view.course_list.delegate

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.CourseContinueView

class CourseContinueViewDelegate(
    private val activity: FragmentActivity,
    private val analytic: Analytic,
    private val screenManager: ScreenManager
) : CourseContinueView {
    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, activity.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(activity.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun showCourse(course: Course, source: CourseViewSource, isAdaptive: Boolean) {
        if (isAdaptive) {
            screenManager.continueAdaptiveCourse(activity, course)
        } else {
            screenManager.showCourseModules(activity, course, source)
        }
    }

    override fun showSteps(course: Course, source: CourseViewSource, lastStep: LastStep) {
        screenManager.continueCourse(activity, course.id, source, lastStep)
    }

    fun onCourseClicked(courseListItem: CourseListItem.Data) {
        analytic.reportEvent(Analytic.Interaction.CLICK_COURSE)
        if (courseListItem.course.enrollment != 0L) {
            screenManager.showCourseModules(activity, courseListItem.course, courseListItem.source)
        } else {
            screenManager.showCourseDescription(activity, courseListItem.course, courseListItem.source)
        }
    }
}