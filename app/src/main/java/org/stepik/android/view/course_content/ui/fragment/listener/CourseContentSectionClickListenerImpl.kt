package org.stepik.android.view.course_content.ui.fragment.listener

import androidx.fragment.app.FragmentManager
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepik.android.presentation.course_content.CourseContentPresenter
import org.stepik.android.view.course.routing.CourseDeepLinkBuilder
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.ui.adapter.delegates.section.CourseContentSectionClickListener
import org.stepik.android.view.course_content.ui.dialog.RemoveCachedContentDialog
import org.stepik.android.view.magic_links.ui.dialog.MagicLinkDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

class CourseContentSectionClickListenerImpl(
    private val courseContentPresenter: CourseContentPresenter,
    private val courseDeepLinkBuilder: CourseDeepLinkBuilder,
    private val childFragmentManager: FragmentManager,
    private val analytic: Analytic
) : CourseContentSectionClickListener {
    override fun onItemClicked(item: CourseContentItem.SectionItem) {
        if (item.section.isExam) {
            val url = courseDeepLinkBuilder
                .createCourseLink(item.section.course, CourseScreenTab.SYLLABUS)

            MagicLinkDialogFragment
                .newInstance(url)
                .showIfNotExists(childFragmentManager, MagicLinkDialogFragment.TAG)
        }
    }

    override fun onItemDownloadClicked(item: CourseContentItem.SectionItem) {
        courseContentPresenter.addSectionDownloadTask(item.section)
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Downloads.STARTED,
            mapOf(
                AmplitudeAnalytic.Downloads.PARAM_CONTENT to AmplitudeAnalytic.Downloads.Values.SECTION
            )
        )
    }

    override fun onItemCancelClicked(item: CourseContentItem.SectionItem) {
        courseContentPresenter.removeSectionDownloadTask(item.section)
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Downloads.CANCELLED,
            mapOf(
                AmplitudeAnalytic.Downloads.PARAM_CONTENT to AmplitudeAnalytic.Downloads.Values.SECTION
            )
        )
    }

    override fun onItemRemoveClicked(item: CourseContentItem.SectionItem) {
        RemoveCachedContentDialog
            .newInstance(section = item.section)
            .showIfNotExists(childFragmentManager, RemoveCachedContentDialog.TAG)
    }
}