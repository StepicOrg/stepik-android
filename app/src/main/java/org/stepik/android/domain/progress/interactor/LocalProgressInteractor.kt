package org.stepik.android.domain.progress.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.assignment.repository.AssignmentRepository
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Assignment
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import ru.nobird.android.core.model.distinct
import ru.nobird.android.core.model.mapToLongArray
import javax.inject.Inject

class LocalProgressInteractor
@Inject
constructor(
    private val progressRepository: ProgressRepository,
    private val assignmentRepository: AssignmentRepository,
    private val stepRepository: StepRepository,
    private val unitRepository: UnitRepository,
    private val sectionRepository: SectionRepository,
    private val courseRepository: CourseRepository,

    private val progressesPublisher: PublishSubject<Progress>
) {
    fun updateStepsProgress(vararg stepIds: Long): Completable =
        stepRepository
            .getSteps(*stepIds)
            .flatMapCompletable(::updateStepsProgress)

    fun updateStepsProgress(steps: List<Step>): Completable =
        getUnits(steps)
            .flatMap { units ->
                zip(getSections(units), getAssignments(steps, units))
                    .flatMap { (sections, assignments) ->
                        getCourses(sections)
                            .map { courses ->
                                assignments.getProgresses() + steps.getProgresses() + units.getProgresses() + sections.getProgresses() + courses.getProgresses()
                            }
                    }
            }
            .flatMap { progressIds ->
                progressRepository
                    .getProgresses(progressIds)
            }
            .doOnSuccess { progresses ->
                progresses.forEach(progressesPublisher::onNext)
            }
            .ignoreElement()

    private fun getAssignments(steps: List<Step>, units: List<Unit>): Single<List<Assignment>> =
        assignmentRepository
            .getAssignments(units.mapNotNull(Unit::assignments).flatten().distinct())
            .map { assignments ->
                assignments
                    .filter { assignment ->
                        steps.any { it.id == assignment.step }
                    }
            }

    private fun getUnits(steps: List<Step>): Single<List<Unit>> =
        steps
            .map(Step::lesson)
            .distinct()
            .toObservable()
            .flatMapSingle { lessonId ->
                unitRepository
                    .getUnitsByLessonId(lessonId)
            }
            .reduce(emptyList()) { a, b -> a + b }

    private fun getSections(units: List<Unit>): Single<List<Section>> =
        sectionRepository
            .getSections(*units.mapToLongArray(Unit::section).distinct())

    private fun getCourses(sections: List<Section>): Single<PagedList<Course>> =
        courseRepository
            .getCourses(*sections.mapToLongArray(Section::course).distinct())
}