package org.stepik.android.domain.course.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.last_step.repository.LastStepRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Course
import javax.inject.Inject

class ContinueLearningInteractor
@Inject
constructor(
    private val lastStepRepository: LastStepRepository,
    private val sectionRepository: SectionRepository,
    private val unitRepository: UnitRepository
) {
    fun getLastStepForCourse(course: Course): Single<LastStep> =
        lastStepRepository
            .getLastStep(course.lastStepId ?: "")
            .switchIfEmpty(resolveCourseFirstStep(course))

    private fun resolveCourseFirstStep(course: Course): Single<LastStep> =
        (course.sections?.firstOrNull()?.let(sectionRepository::getSection) ?: Maybe.empty())
            .flatMap { section ->
                unitRepository.getUnit(section.units.first())
            }
            .map { unit ->
                LastStep(
                    id = "",
                    unit = unit.id,
                    lesson = unit.lesson,
                    step = -1
                )
            }
            .toSingle()
}