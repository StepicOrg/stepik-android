package org.stepik.android.view.course_content.ui.fragment.listener

import android.content.Context
import org.stepic.droid.core.ScreenManager
import org.stepik.android.presentation.course_content.CourseContentPresenter
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.ui.adapter.delegates.section.CourseContentSectionClickListener

class CourseContentSectionClickListenerImpl(
    private val context: Context?,
    private val courseContentPresenter: CourseContentPresenter,
    private val screenManager: ScreenManager
) : CourseContentSectionClickListener {
    override fun onItemClicked(item: CourseContentItem.SectionItem) {
        if (item.section.isExam) {
            screenManager.openSyllabusInWeb(context, item.section.course)
        }
    }

    override fun onItemDownloadClicked(item: CourseContentItem.SectionItem) =
        courseContentPresenter.addSectionDownloadTask(item.section)

    override fun onItemRemoveClicked(item: CourseContentItem.SectionItem) =
        courseContentPresenter.removeSectionDownloadTask(item.section)
}