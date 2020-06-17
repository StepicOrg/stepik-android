package org.stepik.android.view.course_content.model

import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState

sealed class CourseContentItem {
    data class ControlBar(
        val isEnabled: Boolean,
        val personalDeadlinesState: PersonalDeadlinesState,
        val course: Course?,
        val hasDates: Boolean
    ) : CourseContentItem()

    data class SectionItem(
        val section: Section,
        val dates: List<CourseContentSectionDate>,
        val progress: Progress?,
        val isEnabled: Boolean,
        val requiredSection: RequiredSection? = null
    ) : CourseContentItem()

    class UnitItemPlaceholder(
        val unitId: Long
    ) : CourseContentItem()

    data class UnitItem(
        val section: Section,
        val unit: Unit,
        val lesson: Lesson,
        val progress: Progress?,
        val access: Access
    ) : CourseContentItem() {
        enum class Access {
            NO_ACCESS, DEMO, FULL_ACCESS
        }
    }
}