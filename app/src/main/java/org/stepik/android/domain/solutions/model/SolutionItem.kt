package org.stepik.android.domain.solutions.model

import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.Unit

sealed class SolutionItem {
    object Disclaimer : SolutionItem()
    data class SectionItem(
        val section: Section,
        val isEnabled: Boolean
    ) : SolutionItem()

    data class LessonItem(
        val section: Section,
        val unit: Unit,
        val lesson: Lesson,
        val isEnabled: Boolean
    ) : SolutionItem()

    data class SubmissionItem(
        val section: Section,
        val unit: Unit,
        val lesson: Lesson,
        val step: Step,
        val submission: Submission,
        val time: Long,
        val isEnabled: Boolean
    ) : SolutionItem()
}