package org.stepik.android.domain.step.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Maybes.zip
import io.reactivex.rxkotlin.toObservable
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.exam.interactor.ExamSessionDataInteractor
import org.stepik.android.domain.exam.model.SessionData
import ru.nobird.android.domain.rx.toMaybe
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.step.model.StepDirectionData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import org.stepik.android.view.course_content.model.RequiredSection
import ru.nobird.android.domain.rx.filterSingle
import ru.nobird.android.domain.rx.maybeFirst
import java.util.EnumSet
import javax.inject.Inject

class StepNavigationInteractor
@Inject
constructor(
    private val sectionRepository: SectionRepository,
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val examSessionDataInteractor: ExamSessionDataInteractor
) {
    fun getStepNavigationDirections(step: Step, lessonData: LessonData): Single<Set<StepNavigationDirection>> =
        if (lessonData.unit == null ||
            step.position in 2 until lessonData.lesson.steps.size.toLong()) {
            Single.just(EnumSet.noneOf(StepNavigationDirection::class.java))
        } else {
            StepNavigationDirection
                .values()
                .toObservable()
                .filterSingle { isCanMoveInDirection(it, step, lessonData) }
                .reduce(EnumSet.noneOf(StepNavigationDirection::class.java)) { set, direction -> set.add(direction); set }
                .map { it as Set<StepNavigationDirection> }
        }

    fun getStepDirectionData(direction: StepNavigationDirection, step: Step, lessonData: LessonData): Maybe<StepDirectionData> =
        when {
            lessonData.unit == null ||
            lessonData.section == null ||
            lessonData.course == null ||
            !isDirectionCompliesStepPosition(direction, step, lessonData.lesson) ->
                Maybe.empty()

            isDirectionCompliesUnitPosition(direction, lessonData.unit, lessonData.section) ->
                unitRepository
                    .getUnit(lessonData.section.units[
                        when (direction) {
                            StepNavigationDirection.NEXT ->
                                lessonData.unit.position

                            StepNavigationDirection.PREV ->
                                lessonData.unit.position - 2
                        }
                    ])
                    .flatMap { unit ->
                        lessonRepository
                            .getLesson(unit.lesson)
                            .map { lesson ->
                                lessonData.copy(unit = unit, lesson = lesson)
                            }
                    }

            else ->
                getSlicedSections(direction, lessonData.section, lessonData.course)
                    .flatMapMaybe { sections ->
                        sections
                            .firstOrNull() { it.units.isNotEmpty() }
                            .toMaybe()
                    }
                    .flatMap { section ->
                        val unitId =
                            when (direction) {
                                StepNavigationDirection.NEXT ->
                                    section.units.first()

                                StepNavigationDirection.PREV ->
                                    section.units.last()
                            }

                        unitRepository
                            .getUnit(unitId)
                            .flatMap { unit ->
                                lessonRepository
                                    .getLesson(unit.lesson)
                                    .map { lesson ->
                                        lessonData.copy(section = section, unit = unit, lesson = lesson)
                                    }
                            }
                    }
        }.flatMap {
            val requiredSectionSource =
                if (it.section?.isRequirementSatisfied == false) {
                    getRequiredSection(it.section.requiredSection).onErrorReturnItem(RequiredSection.EMPTY)
                } else {
                    Maybe.just(RequiredSection.EMPTY)
                }

            val examSessionSource =
                if (it.section != null) {
                    examSessionDataInteractor.getSessionData(it.section, DataSourceType.REMOTE).onErrorReturnItem(SessionData.EMPTY)
                } else {
                    Single.just(SessionData.EMPTY)
                }

            zip(
                requiredSectionSource,
                examSessionSource.toMaybe()
            ) { requiredSection, examSessionData ->
                StepDirectionData(lessonData = it, requiredSection = requiredSection, examSessionData = examSessionData)
            }
        }

    private fun getRequiredSection(sectionId: Long): Maybe<RequiredSection> =
        sectionRepository
            .getSection(sectionId, DataSourceType.CACHE)
            .flatMap { section ->
                progressRepository
                    .getProgresses(listOfNotNull(section.progress), primarySourceType = DataSourceType.REMOTE)
                    .maybeFirst()
                    .map { progress ->
                        RequiredSection(section, progress)
                    }
            }

    private fun isCanMoveInDirection(direction: StepNavigationDirection, step: Step, lessonData: LessonData): Single<Boolean> =
        when {
            lessonData.unit == null ||
            lessonData.section == null ||
            lessonData.course == null ||
            !isDirectionCompliesStepPosition(direction, step, lessonData.lesson) ->
                Single.just(false)

            isDirectionCompliesUnitPosition(direction, lessonData.unit, lessonData.section) ->
                Single.just(true)

            else ->
                getSlicedSections(direction, lessonData.section, lessonData.course)
                    .map { sections ->
                        sections.any { it.units.isNotEmpty() }
                    }
        }

    private fun isDirectionCompliesStepPosition(direction: StepNavigationDirection, step: Step, lesson: Lesson): Boolean =
        direction == StepNavigationDirection.PREV && step.position == 1L ||
        direction == StepNavigationDirection.NEXT && step.position == lesson.steps.size.toLong()

    private fun isDirectionCompliesUnitPosition(direction: StepNavigationDirection, unit: Unit, section: Section): Boolean =
        direction == StepNavigationDirection.PREV && unit.position > 1 ||
        direction == StepNavigationDirection.NEXT && unit.position < section.units.size

    private fun getSlicedSections(direction: StepNavigationDirection, section: Section, course: Course): Single<List<Section>> {
        val sectionIds = course.sections ?: return Single.just(emptyList())

        val range =
            when (direction) {
                StepNavigationDirection.NEXT ->
                    (section.position until sectionIds.size)

                StepNavigationDirection.PREV ->
                    (0 until section.position - 1)
            }

        return sectionRepository
            .getSections(sectionIds.slice(range))
            .map { sections ->
                when (direction) {
                    StepNavigationDirection.NEXT ->
                        sections

                    StepNavigationDirection.PREV ->
                        sections.asReversed()
                }
            }
    }
}