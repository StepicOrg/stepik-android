package org.stepik.android.presentation.course_content.mapper

import org.stepic.droid.R
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_content.CourseContentView
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import javax.inject.Inject

class CourseContentStateMapper
@Inject
constructor(
    private val sectionDatesMapper: CourseContentSectionDatesMapper
) {
    fun mergeStateWithCourseContent(state: CourseContentView.State, course: Course, courseContent: List<CourseContentItem>): CourseContentView.State {
        val personalDeadlinesState =
            if (course.enrollment == 0L) {
                PersonalDeadlinesState.NoDeadlinesNeeded
            } else {
                (state as? CourseContentView.State.CourseContentLoaded)
                    ?.personalDeadlinesState
                    ?.takeUnless { it is PersonalDeadlinesState.NoDeadlinesNeeded }
                    ?: PersonalDeadlinesState.Idle
            }

        val content =
            (personalDeadlinesState as? PersonalDeadlinesState.Deadlines)
                ?.record
                ?.let { applyDeadlinesToCourseContent(courseContent, it) }
                ?: courseContent

        return CourseContentView.State.CourseContentLoaded(course, personalDeadlinesState, content)
    }

    fun mergeStateWithPersonalDeadlines(state: CourseContentView.State, deadlinesRecord: StorageRecord<DeadlinesWrapper>?): CourseContentView.State {
        if (state !is CourseContentView.State.CourseContentLoaded) return state

        state
            .courseContent
            .takeIf { items ->
                items
                    .asSequence()
                    .filterIsInstance<CourseContentItem.SectionItem>()
                    .all { item ->
                        item.section.softDeadline == null && item.section.hardDeadline == null
                    }
            }
            ?: return state.copy(personalDeadlinesState = PersonalDeadlinesState.NoDeadlinesNeeded)

        val personalDeadlinesState = deadlinesRecord
            ?.let(PersonalDeadlinesState::Deadlines)
            ?: PersonalDeadlinesState.EmptyDeadlines

        val courseContent = applyDeadlinesToCourseContent(state.courseContent, deadlinesRecord)

        return state.copy(personalDeadlinesState = personalDeadlinesState, courseContent = courseContent)
    }

    private fun applyDeadlinesToCourseContent(courseContent: List<CourseContentItem>, deadlinesRecord: StorageRecord<DeadlinesWrapper>?): List<CourseContentItem> =
        courseContent
            .map { item ->
                if (item is CourseContentItem.SectionItem) {
                    val dates = deadlinesRecord
                        ?.data
                        ?.deadlines
                        ?.find { it.section == item.section.id }
                        ?.let { deadline ->
                            listOf(CourseContentSectionDate(R.string.course_content_timeline_deadline, deadline.deadline))
                        }
                        ?: sectionDatesMapper.mapSectionDates(item.section)
                    item.copy(dates = dates)
                } else {
                    item
                }
            }
}