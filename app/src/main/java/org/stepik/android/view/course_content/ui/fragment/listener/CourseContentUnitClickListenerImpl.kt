package org.stepik.android.view.course_content.ui.fragment.listener

import android.app.Activity
import androidx.fragment.app.FragmentManager
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepik.android.presentation.course_content.CourseContentPresenter
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitClickListener
import org.stepik.android.view.course_content.ui.dialog.RemoveCachedContentDialog

class CourseContentUnitClickListenerImpl(
    private val activity: Activity?,
    private val courseContentPresenter: CourseContentPresenter,
    private val screenManager: ScreenManager,
    private val childFragmentManager: FragmentManager,
    private val analytic: Analytic
) : CourseContentUnitClickListener {
    override fun onItemClicked(item: CourseContentItem.UnitItem) {
        screenManager.showSteps(activity, item.unit, item.lesson, item.section)
    }

    override fun onItemDownloadClicked(item: CourseContentItem.UnitItem) {
        courseContentPresenter.addUnitDownloadTask(item.unit)
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Downloads.STARTED,
            mapOf(
                AmplitudeAnalytic.Downloads.PARAM_CONTENT to AmplitudeAnalytic.Downloads.Values.LESSON
            )
        )
    }

    override fun onItemCancelClicked(item: CourseContentItem.UnitItem) {
        courseContentPresenter.removeUnitDownloadTask(item.unit)
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Downloads.CANCELLED,
            mapOf(
                AmplitudeAnalytic.Downloads.PARAM_CONTENT to AmplitudeAnalytic.Downloads.Values.LESSON
            )
        )
    }

    override fun onItemRemoveClicked(item: CourseContentItem.UnitItem) {
        val fragmentManager = childFragmentManager
            .takeIf { it.findFragmentByTag(RemoveCachedContentDialog.TAG) == null }
            ?: return

        RemoveCachedContentDialog
            .newInstance(unit = item.unit)
            .show(fragmentManager, RemoveCachedContentDialog.TAG)
    }
}