package org.stepik.android.domain.attempts.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Lesson
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.Unit
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.attempts.mapper.AttemptCacheItemMapper
import org.stepik.android.view.attempts.model.AttemptCacheItem
import javax.inject.Inject

class AttemptsInteractor
@Inject
constructor(
    private val attemptRepository: AttemptRepository,
    private val submissionRepository: SubmissionRepository,
    private val stepRepository: StepRepository,
    private val lessonRepository: LessonRepository,
    private val sectionRepository: SectionRepository,
    private val unitRepository: UnitRepository,

    private val attemptCacheItemMapper: AttemptCacheItemMapper
) {

    fun fetchAttemptCacheItems(): Single<List<AttemptCacheItem>> =
        getAttempts()

    private fun getAttempts(): Single<List<AttemptCacheItem>> =
        attemptRepository
            .getAttempts(dataSourceType = DataSourceType.CACHE)
            .flatMap { attempts ->  attempts
                .toObservable()
                .flatMapSingle { attempt -> submissionRepository.getSubmissionsForAttempt(attempt.id, dataSourceType = DataSourceType.CACHE) }
                .reduce(emptyList<Submission>()) { a, b -> a + b }
                .flatMap { submissions ->
                    getSteps(attempts.mapToLongArray { it.step }, attempts, submissions.filter { it.status == Submission.Status.LOCAL }) }
            }

    fun removeAttempts(attemptIds: List<Long>): Completable =
        Completable.fromAction {
            submissionDao.removeAllInRange(DbStructureSubmission.Columns.ATTEMPT_ID, attemptIds.mapToLongArray { it }.joinToString())
        }

    private fun getSteps(ids: LongArray, attempts: List<Attempt>, submissions: List<Submission>): Single<List<AttemptCacheItem>> =
        stepRepository
            .getSteps(*ids, primarySourceType = DataSourceType.CACHE)
            .flatMap { steps -> getLessons(steps.mapToLongArray { it.lesson }, attempts, submissions, steps) }

    private fun getLessons(ids: LongArray, attempts: List<Attempt>, submissions: List<Submission>, steps: List<Step>): Single<List<AttemptCacheItem>> =
        lessonRepository
            .getLessons(*ids, primarySourceType = DataSourceType.CACHE)
            .flatMap { lessons -> lessons
                .toObservable()
                .flatMapSingle { lesson -> unitRepository.getUnitsByLessonId(lesson.id, primarySourceType = DataSourceType.CACHE) }
                .reduce(emptyList<Unit>()) { a, b ->  a + b }
                .flatMap { units -> getSections(units.mapToLongArray { it.section }, attempts, submissions, steps, lessons, units) }
            }

    private fun getSections(ids: LongArray, attempts: List<Attempt>, submissions: List<Submission>, steps: List<Step>, lessons: List<Lesson>, units: List<Unit>): Single<List<AttemptCacheItem>> =
        sectionRepository
            .getSections(*ids, primarySourceType = DataSourceType.CACHE)
            .map { sections ->
                attemptCacheItemMapper.mapAttemptCacheItems(attempts, submissions, steps, lessons, units, sections)
            }
}