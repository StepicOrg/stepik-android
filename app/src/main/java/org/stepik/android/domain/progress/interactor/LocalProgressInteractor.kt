package org.stepik.android.domain.progress.interactor

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.util.getProgresses
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
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
    private val unitRepository: UnitRepository,
    private val sectionRepository: SectionRepository,
    private val courseRepository: CourseRepository,

    private val progressesPublisher: PublishSubject<Progress>
) {
    fun updateStepProgress(step: Step): Completable =
        unitRepository
            .getUnitsByLessonId(step.lesson)
            .flatMap { units ->
                val sectionIds = units.mapToLongArray(Unit::section)
                sectionRepository
                    .getSections(*sectionIds)
                    .flatMap { sections ->
                        val coursesIds = sections.mapToLongArray(Section::course)
                        courseRepository
                            .getCourses(*coursesIds)
                            .map { courses ->
                                units.getProgresses() + sections.getProgresses() + courses.getProgresses()
                            }
                    }
            }
            .flatMap { progressIds ->
                progressRepository
                    .getProgresses(*progressIds, step.progress ?: "")
            }
            .doOnSuccess { progresses ->
                progresses.forEach(progressesPublisher::onNext)
            }
            .toCompletable()
}