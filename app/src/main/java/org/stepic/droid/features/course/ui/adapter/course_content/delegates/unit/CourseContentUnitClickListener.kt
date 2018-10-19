package org.stepic.droid.features.course.ui.adapter.course_content.delegates.unit

import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem

interface CourseContentUnitClickListener {
    fun onItemClicked(item: CourseContentItem.UnitItem)
    fun onItemDownloadClicked(item: CourseContentItem.UnitItem)
    fun onItemRemoveClicked(item: CourseContentItem.UnitItem)
}