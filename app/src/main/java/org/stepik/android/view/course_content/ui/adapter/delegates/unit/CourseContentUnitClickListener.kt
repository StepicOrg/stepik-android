package org.stepik.android.view.course_content.ui.adapter.delegates.unit

import org.stepik.android.view.course_content.model.CourseContentItem

interface CourseContentUnitClickListener {
    fun onItemClicked(item: CourseContentItem.UnitItem)
    fun onItemDownloadClicked(item: CourseContentItem.UnitItem)
    fun onItemCancelClicked(item: CourseContentItem.UnitItem)
    fun onItemRemoveClicked(item: CourseContentItem.UnitItem)
}