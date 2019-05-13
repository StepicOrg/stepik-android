package org.stepik.android.domain.lesson.interactor

import io.reactivex.Maybe
import io.reactivex.rxkotlin.Maybes.zip
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.domain.lesson.model.LessonDeepLinkData
import javax.inject.Inject

class LessonInteractor
@Inject
constructor(
    private val lessonRepository: LessonRepository,
    private val unitRepository: UnitRepository,
    private val sectionRepository: SectionRepository
) {
    fun getLessonData(lesson: Lesson, unit: Unit, section: Section): Maybe<LessonData> =
        zip(
            lessonRepository.getLesson(lesson.id).onErrorReturnItem(lesson),
            unitRepository.getUnit(unit.id).onErrorReturnItem(unit),
            sectionRepository.getSection(section.id).onErrorReturnItem(section)
        )
            .map { (lesson, unit, section) ->
                LessonData(lesson, unit, section)
            }

    fun getLessonData(lastStep: LastStep): Maybe<LessonData> =
        zip(
            lessonRepository.getLesson(lastStep.lesson),
            unitRepository.getUnit(lastStep.unit)
        )
            .flatMap { (lesson, unit) ->
                sectionRepository
                    .getSection(unit.section)
                    .map { section ->
                        LessonData(lesson, unit, section, lesson.steps.indexOf(lastStep.step).toLong() + 1)
                    }
            }

    fun getLessonData(lessonDeepLinkData: LessonDeepLinkData): Maybe<LessonData> =
        zip(
            lessonRepository.getLesson(lessonDeepLinkData.lessonId),
            if (lessonDeepLinkData.unitId == null) {
                unitRepository
                    .getUnitsByLessonId(lessonDeepLinkData.lessonId)
                    .maybeFirst()
            } else {
                unitRepository
                    .getUnit(lessonDeepLinkData.unitId)
            }
        )
            .flatMap { (lesson, unit) ->
                sectionRepository
                    .getSection(unit.section)
                    .map { section ->
                        LessonData(lesson, unit, section, lessonDeepLinkData.stepPosition)
                    }
            }
}