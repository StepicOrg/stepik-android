package org.stepik.android.domain.solutions.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import ru.nobird.android.core.model.mapToLongArray
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.solutions.mapper.SolutionItemMapper
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Lesson
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.Unit
import org.stepik.android.model.attempts.Attempt
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SolutionsInteractor
@Inject
constructor(
    private val attemptRepository: AttemptRepository,
    private val submissionRepository: SubmissionRepository,
    private val stepRepository: StepRepository,
    private val lessonRepository: LessonRepository,
    private val sectionRepository: SectionRepository,
    private val unitRepository: UnitRepository,

    private val solutionItemMapper: SolutionItemMapper
) {
    fun sendSubmissions(submissionItems: List<SolutionItem.SubmissionItem>): Observable<Pair<Step, Submission>> =
        submissionItems.map { it.step to it.submission }
            .toObservable()
            .concatMapEager { (step, submission) ->
                submissionRepository
                    .createSubmission(submission)
                    .flatMapObservable {
                        Observable
                            .interval(1, TimeUnit.SECONDS)
                            .flatMapMaybe { submissionRepository.getSubmissionsForAttempt(submission.attempt).maybeFirst() }
                            .skipWhile { it.status == Submission.Status.EVALUATION }
                    }
                    .take(1)
                    .map { newSubmission ->
                        step to newSubmission
                    }
            }

    fun fetchAttemptCacheItems(courseId: Long, localOnly: Boolean): Single<List<SolutionItem>> =
        getAttempts(courseId, localOnly)

    private fun getAttempts(courseId: Long, localOnly: Boolean): Single<List<SolutionItem>> =
        attemptRepository
            .getAttempts(dataSourceType = DataSourceType.CACHE)
            .flatMap { attempts ->  attempts
                .toObservable()
                .flatMapSingle { attempt -> submissionRepository.getSubmissionsForAttempt(attempt.id, dataSourceType = DataSourceType.CACHE) }
                .reduce(emptyList<Submission>()) { a, b -> a + b }
                .flatMap { submissions ->
                    val submissionsParameter = if (localOnly) {
                        submissions.filter { it.status == Submission.Status.LOCAL }
                    } else {
                        submissions
                    }
                    getSteps(courseId, attempts.mapToLongArray { it.step }, attempts, submissionsParameter) }
            }

    fun removeAttempts(attemptIds: List<Long>): Completable =
        attemptIds
            .toObservable()
            .flatMapCompletable { attemptId -> submissionRepository.removeSubmissionsForAttempt(attemptId) }

    private fun getSteps(courseId: Long, ids: LongArray, attempts: List<Attempt>, submissions: List<Submission>): Single<List<SolutionItem>> =
        stepRepository
            .getSteps(*ids, primarySourceType = DataSourceType.CACHE)
            .flatMap { steps -> getLessons(courseId, steps.mapToLongArray { it.lesson }, attempts, submissions, steps) }

    private fun getLessons(courseId: Long, ids: LongArray, attempts: List<Attempt>, submissions: List<Submission>, steps: List<Step>): Single<List<SolutionItem>> =
        lessonRepository
            .getLessons(*ids, primarySourceType = DataSourceType.CACHE)
            .flatMap { lessons -> lessons
                .toObservable()
                .flatMapSingle { lesson -> unitRepository.getUnitsByLessonId(lesson.id, primarySourceType = DataSourceType.CACHE) }
                .reduce(emptyList<Unit>()) { a, b ->  a + b }
                .flatMap { units -> getSections(courseId, units.mapToLongArray { it.section }, attempts, submissions, steps, lessons, units) }
            }

    private fun getSections(courseId: Long, ids: LongArray, attempts: List<Attempt>, submissions: List<Submission>, steps: List<Step>, lessons: List<Lesson>, units: List<Unit>): Single<List<SolutionItem>> =
        sectionRepository
            .getSections(*ids, primarySourceType = DataSourceType.CACHE)
            .map { sections ->
                solutionItemMapper.mapAttemptCacheItems(courseId, attempts, submissions, steps, lessons, units, sections)
            }
}