package org.stepik.android.view.course_content.ui.adapter.delegates.section

import org.stepik.android.view.course_content.model.CourseContentItem

interface CourseContentSectionClickListener {
    fun onItemDownloadClicked(item: CourseContentItem.SectionItem)
    fun onItemRemoveClicked(item: CourseContentItem.SectionItem)
}