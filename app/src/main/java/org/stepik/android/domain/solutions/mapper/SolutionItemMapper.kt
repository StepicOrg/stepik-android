package org.stepik.android.domain.solutions.mapper

import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.Unit
import org.stepik.android.model.attempts.Attempt
import javax.inject.Inject

class SolutionItemMapper
@Inject
constructor() {
    fun mapAttemptCacheItems(
        courseId: Long,
        attempts: List<Attempt>,
        submissions: List<Submission>,
        steps: List<Step>,
        lessons: List<Lesson>,
        units: List<Unit>,
        sections: List<Section>
    ): List<SolutionItem> {
        val sectionsMap = sections.asSequence().filter { it.course == courseId }.associateBy(Section::id)
        val unitsMap = units.asSequence().filter { it.section in sectionsMap }.associateBy(Unit::lesson)
        val lessonsMap = lessons.asSequence().filter { lesson -> unitsMap.any { it.value.lesson == lesson.id } }.associateBy(Lesson::id)
        val stepsMap = steps.asSequence().filter { it.lesson in lessonsMap }.associateBy(Step::id)
        val attemptsMap = attempts.asSequence().filter { it.step in stepsMap }.associateBy(Attempt::id)

        val items = submissions.mapNotNull { submission ->
            val attempt = attemptsMap[submission.attempt] ?: return@mapNotNull null
            val lessonId = stepsMap[attempt.step]?.lesson ?: return@mapNotNull null
            val lesson = lessonsMap[lessonId] ?: return@mapNotNull null
            val unit = unitsMap[lessonId] ?: return@mapNotNull null
            val section = sectionsMap[unit.section] ?: return@mapNotNull null
            val step = stepsMap[attempt.step] ?: return@mapNotNull null
            SolutionItem.SubmissionItem(
                section = section,
                unit = unit,
                lesson = lesson,
                step = step,
                submission = submission,
                time = attempt.time?.time ?: 0L,
                isEnabled = true
            )
        }

        val lessonItems = items.map { SolutionItem.LessonItem(it.section, it.unit, it.lesson, true) }.distinct().sortedBy { it.lesson.id }
        val sectionItems = lessonItems.map { SolutionItem.SectionItem(it.section, true) }.distinct().sortedBy { it.section.id }

        val attemptCacheItems = mutableListOf<SolutionItem>()

        sectionItems.forEach { sectionItem ->
            attemptCacheItems.add(sectionItem)
            val lessonsBySection = lessonItems.filter { it.section == sectionItem.section }
            lessonsBySection.forEach { lessonItem ->
                attemptCacheItems.add(lessonItem)
                val submissionItems = items.filter { it.lesson == lessonItem.lesson }.sortedBy { it.step.position }
                attemptCacheItems += submissionItems
            }
        }
        return attemptCacheItems
    }
}