package org.stepik.android.view.attempts.model

import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.Unit
import java.util.Date

sealed class AttemptCacheItem {
    data class SectionItem(
        val section: Section
    ) : AttemptCacheItem()

    data class LessonItem(
        val section: Section,
        val unit: Unit,
        val lesson: Lesson
    ) : AttemptCacheItem()

    data class SubmissionItem(
        val section: Section,
        val unit: Unit,
        val lesson: Lesson,
        val step: Step,
        val submission: Submission,
        val time: Date
    ) : AttemptCacheItem()
}