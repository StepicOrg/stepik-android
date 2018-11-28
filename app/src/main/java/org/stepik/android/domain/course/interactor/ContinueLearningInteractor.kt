package org.stepik.android.domain.course.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.util.hasUserAccessAndNotEmpty
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.last_step.repository.LastStepRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Section
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
        getFirstAvailableSection(course)
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

    private fun getFirstAvailableSection(course: Course): Maybe<Section> =
        course.sections
            ?.toObservable()
            ?.flatMapMaybe(sectionRepository::getSection)
            ?.filter { it.hasUserAccessAndNotEmpty(course) }
            ?.firstElement()
            ?: Maybe.empty()
}