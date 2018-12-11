package org.stepik.android.view.course_content.model

import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

sealed class CourseContentItem {
    object ControlBar : CourseContentItem()

    data class SectionItem(
            val section: Section,
            val dates: List<CourseContentSectionDate>,
            val progress: Progress?,
            val isEnabled: Boolean
    ) : CourseContentItem()

    class UnitItemPlaceholder(
        val unitId: Long
    ) : CourseContentItem()

    data class UnitItem(
            val section: Section,
            val unit: Unit,
            val lesson: Lesson,
            val progress: Progress?,
            val isEnabled: Boolean
    ) : CourseContentItem()
}