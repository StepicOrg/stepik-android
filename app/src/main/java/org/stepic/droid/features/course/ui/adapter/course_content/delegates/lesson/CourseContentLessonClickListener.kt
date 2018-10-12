package org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson

import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem

interface CourseContentLessonClickListener {
    fun onItemClicked(item: CourseContentItem.LessonItem)
    fun onItemDownloadClicked(item: CourseContentItem.LessonItem)
    fun onItemRemoveClicked(item: CourseContentItem.LessonItem)
}