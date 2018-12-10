package org.stepik.android.presentation.course_content

import org.stepik.android.view.course_content.model.CourseContentItem

interface CourseContentView {
    fun setCourseContent(items: List<CourseContentItem>)
}