package org.stepik.android.domain.attempts.model

import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.Unit

sealed class AttemptCacheItem {
    data class SectionItem(
        val section: Section,
        val isEnabled: Boolean
    ) : AttemptCacheItem()

    data class LessonItem(
        val section: Section,
        val unit: Unit,
        val lesson: Lesson,
        val isEnabled: Boolean
    ) : AttemptCacheItem()

    data class SubmissionItem(
        val section: Section,
        val unit: Unit,
        val lesson: Lesson,
        val step: Step,
        val submission: Submission,
        val time: Long,
        val isEnabled: Boolean
    ) : AttemptCacheItem()
}