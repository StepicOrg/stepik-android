package org.stepik.android.domain.course.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.course.model.CourseLastStep
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
    fun getLastStepForCourse(course: Course): Single<CourseLastStep> =
        lastStepRepository.getLastStep(course.lastStepId ?: "")
            .map { lastStep ->
                CourseLastStep(
                    courseId = course.id,
                    unitId = lastStep.unit,
                    lessonId = lastStep.lesson,
                    stepId = lastStep.step
                )
            }.switchIfEmpty(
                (course.sections?.firstOrNull()?.let(sectionRepository::getSection) ?: Maybe.empty())
                    .flatMap { section ->
                        unitRepository.getUnit(section.units.first())
                    }
                    .map { unit ->
                        CourseLastStep(
                            courseId = course.id,
                            unitId = unit.id,
                            lessonId = unit.lesson,
                            stepId = -1
                        )
                    }
            ).toSingle()
}