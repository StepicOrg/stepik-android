package org.stepik.android.presentation.attempts.mapper

import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.Unit
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.attempts.model.AttemptCacheItem
import javax.inject.Inject

class AttemptCacheItemMapper
@Inject
constructor() {
    fun mapAttemptCacheItems(
        attempts: List<Attempt>,
        submissions: List<Submission>,
        steps: List<Step>,
        lessons: List<Lesson>,
        units: List<Unit>,
        sections: List<Section>
    ): List<AttemptCacheItem> {
        val items = submissions.map { submission ->
            val attempt = attempts.find { it.id == submission.attempt }
            val lessonId = steps.find { it.id == attempt?.step }?.lesson
            val lesson = lessons.find { it.id == lessonId }

            val unit = units.find { it.lesson == lessonId }
            val section = sections.find { it.id == unit?.section }
            AttemptCacheItem.SubmissionItem(section = section!!, unit = unit!!, lesson = lesson!!, submission = submission, time = attempt?.time!!)
        }

        val lessonItems = items.map { AttemptCacheItem.LessonItem(it.section, it.unit, it.lesson) }.distinct().sortedBy { it.lesson.id }
        val sectionItems = lessonItems.map { AttemptCacheItem.SectionItem(it.section) }.distinct().sortedBy { it.section.id }

        val attemptCacheItems = mutableListOf<AttemptCacheItem>()

        sectionItems.forEach { sectionItem ->
            attemptCacheItems.add(sectionItem)
            val lessonsBySection = lessonItems.filter { it.section == sectionItem.section }
            lessonsBySection.forEach { lessonItem ->
                attemptCacheItems.add(lessonItem)
                val subs = items.filter { it.lesson == lessonItem.lesson }
                attemptCacheItems += subs
            }
        }
        return attemptCacheItems
    }
}