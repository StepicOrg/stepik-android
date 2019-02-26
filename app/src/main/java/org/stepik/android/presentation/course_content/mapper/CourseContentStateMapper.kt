package org.stepik.android.presentation.course_content.mapper

import org.stepic.droid.R
import org.stepic.droid.analytic.experiments.PersonalDeadlinesSplitTest
import org.stepic.droid.util.isNullOrEmpty
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.presentation.course_content.CourseContentView
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import javax.inject.Inject

class CourseContentStateMapper
@Inject
constructor(
    private val personalDeadlinesSplitTest: PersonalDeadlinesSplitTest,
    private val sectionDatesMapper: CourseContentSectionDatesMapper
) {
    fun mergeStateWithCourseContent(state: CourseContentView.State, course: Course, courseContent: List<CourseContentItem>): CourseContentView.State {
        if (courseContent.isEmpty()) {
            return if (course.sections.isNullOrEmpty()) {
                CourseContentView.State.EmptyContent
            } else {
                CourseContentView.State.Loading
            }
        }

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

        return when (personalDeadlinesSplitTest.currentGroup.isPersonalDeadlinesEnabled) {
            true -> CourseContentView.State.CourseContentLoaded(course, personalDeadlinesState, content)
            false -> CourseContentView.State.CourseContentLoaded(course, PersonalDeadlinesState.NoDeadlinesNeeded, content)
        }
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

    fun mergeStateWithProgress(state: CourseContentView.State, progress: Progress): CourseContentView.State =
        if (state !is CourseContentView.State.CourseContentLoaded ||
            state.courseContent.all { !isItemContainsProgress(it, progress) }) {
            state
        } else {
            state.copy(courseContent = state.courseContent.map { updateItemProgress(it, progress) })
        }

    private fun isItemContainsProgress(item: CourseContentItem, progress: Progress): Boolean =
        when (item) {
            is CourseContentItem.SectionItem ->
                item.progress?.id == progress.id

            is CourseContentItem.UnitItem ->
                item.progress?.id == progress.id

            else ->
                false
        }

    private fun updateItemProgress(item: CourseContentItem, progress: Progress): CourseContentItem =
        when (item) {
            is CourseContentItem.SectionItem ->
                item.takeIf { it.progress?.id == progress.id }
                    ?.copy(progress = progress)

            is CourseContentItem.UnitItem ->
                item.takeIf { it.progress?.id == progress.id }
                    ?.copy(progress = progress)

            else ->
                null
        } ?: item
}