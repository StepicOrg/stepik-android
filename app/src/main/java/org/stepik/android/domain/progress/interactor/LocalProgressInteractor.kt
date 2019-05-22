package org.stepik.android.domain.progress.interactor

import io.reactivex.Completable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.util.getProgresses
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import javax.inject.Inject

class LocalProgressInteractor
@Inject
constructor(
    private val progressRepository: ProgressRepository,
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
        steps
            .map(Step::lesson)
            .distinct()
            .toObservable()
            .flatMapSingle { lessonId ->
                unitRepository
                    .getUnitsByLessonId(lessonId)
            }
            .reduce(emptyList<Unit>()) { a, b -> a + b }
            .flatMap { units ->
                val sectionIds = units.mapToLongArray(Unit::section)
                sectionRepository
                    .getSections(*sectionIds)
                    .flatMap { sections ->
                        val coursesIds = sections.mapToLongArray(Section::course)
                        courseRepository
                            .getCourses(*coursesIds)
                            .map { courses ->
                                steps.getProgresses() + units.getProgresses() + sections.getProgresses() + courses.getProgresses()
                            }
                    }
            }
            .flatMap { progressIds ->
                progressRepository
                    .getProgresses(*progressIds)
            }
            .doOnSuccess { progresses ->
                progresses.forEach(progressesPublisher::onNext)
            }
            .toCompletable()
}