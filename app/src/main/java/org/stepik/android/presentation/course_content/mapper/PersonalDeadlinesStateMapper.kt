package org.stepik.android.presentation.course_content.mapper

import org.stepic.droid.R
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.presentation.course_content.CourseContentView
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import javax.inject.Inject

class PersonalDeadlinesStateMapper
@Inject
constructor() {
    fun mergeCourseContentStateWithPersonalDeadlines(state: CourseContentView.State, deadlinesRecord: StorageRecord<DeadlinesWrapper>?): CourseContentView.State {
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

        deadlinesRecord
            ?: return state.copy(personalDeadlinesState = PersonalDeadlinesState.EmptyDeadlines)

        val courseContent = state
            .courseContent
            .map { item ->
                if (item is CourseContentItem.SectionItem) {
                    deadlinesRecord
                        .data
                        .deadlines
                        .find { it.section == item.section.id }
                        ?.let { deadline ->
                            val dates = (item.dates + CourseContentSectionDate(R.string.course_content_timeline_deadline, deadline.deadline))
                                .sortedBy { it.date }

                            item.copy(dates = dates)
                        }
                        ?: item
                } else {
                    item
                }
            }

        return state.copy(personalDeadlinesState = PersonalDeadlinesState.Deadlines(deadlinesRecord), courseContent = courseContent)
    }
}