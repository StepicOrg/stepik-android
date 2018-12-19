package org.stepik.android.view.course_content.ui.fragment.listener

import org.stepik.android.presentation.course_content.CourseContentPresenter
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.ui.adapter.delegates.section.CourseContentSectionClickListener

class CourseContentSectionClickListenerImpl(
    private val courseContentPresenter: CourseContentPresenter
) : CourseContentSectionClickListener {
    override fun onItemDownloadClicked(item: CourseContentItem.SectionItem) =
        courseContentPresenter.addSectionDownloadTask(item.section)

    override fun onItemRemoveClicked(item: CourseContentItem.SectionItem) =
        courseContentPresenter.removeSectionDownloadTask(item.section)
}