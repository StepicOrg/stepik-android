package org.stepik.android.view.attempts.model

import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Submission
import java.util.Date

sealed class AttemptCacheItem {
    data class SectionItem(
        val section: Section
    ) : AttemptCacheItem()

    data class LessonItem(
        val section: Section,
        val lesson: Lesson
    ) : AttemptCacheItem()

    data class SubmissionItem(
        val section: Section,
        val lesson: Lesson,
        val submission: Submission,
        val time: Date
    ) : AttemptCacheItem()
}