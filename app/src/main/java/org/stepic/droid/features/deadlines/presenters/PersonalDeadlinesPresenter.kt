package org.stepic.droid.features.deadlines.presenters

import android.os.Bundle
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.features.deadlines.util.DeadlinesResolver
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.features.deadlines.model.Deadline
import org.stepic.droid.model.Course
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.features.deadlines.model.LearningRate
import org.stepic.droid.features.deadlines.presenters.contracts.PersonalDeadlinesView
import org.stepic.droid.features.deadlines.repository.DeadlinesRepository
import org.stepic.droid.util.addDisposable
import javax.inject.Inject

class PersonalDeadlinesPresenter
@Inject
constructor(
        private val analytic: Analytic,
        private val deadlinesResolver: DeadlinesResolver,
        private val deadlinesRepository: DeadlinesRepository,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
): PresenterBase<PersonalDeadlinesView>() {
    private val compositeDisposable = CompositeDisposable()

    private var courseId = -1L // only for analytics

    private var state: PersonalDeadlinesView.State = PersonalDeadlinesView.State.Idle
        set(value) {
            field = value
            setStateToView(field)
        }

    fun fetchDeadlinesForCourse(course: Course?, force: Boolean = false) {
        if (course == null) {
            state = PersonalDeadlinesView.State.EmptyDeadlines
            return
        }

        courseId = course.courseId

        if (state == PersonalDeadlinesView.State.Idle || force) {
            state = PersonalDeadlinesView.State.Loading
            compositeDisposable addDisposable deadlinesRepository.getDeadlinesForCourse(course.courseId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                            onError = { state = PersonalDeadlinesView.State.Error },
                            onComplete = { state = PersonalDeadlinesView.State.EmptyDeadlines },
                            onSuccess = { state = PersonalDeadlinesView.State.Deadlines(it) }
                    )
        } else {
            setStateToView(state)
        }
    }

    fun createDeadlinesForCourse(course: Course?, learningRate: LearningRate) {
        if (course == null || state != PersonalDeadlinesView.State.EmptyDeadlines) return
        state = PersonalDeadlinesView.State.Loading
        compositeDisposable addDisposable deadlinesResolver.calculateDeadlinesForCourse(course.courseId, learningRate)
                .flatMap { deadlinesRepository.createDeadlinesForCourse(it) }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { state = PersonalDeadlinesView.State.Error },
                        onSuccess = { state = PersonalDeadlinesView.State.Deadlines(it) }
                )
    }

    fun updateDeadlines(deadlines: List<Deadline>) {
        val record = (state as? PersonalDeadlinesView.State.Deadlines)?.record ?: return
        val newRecord = record.copy(data = DeadlinesWrapper(record.data.course, deadlines))
        state = PersonalDeadlinesView.State.Loading
        compositeDisposable addDisposable deadlinesRepository.updateDeadlinesForCourse(newRecord)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { state = PersonalDeadlinesView.State.Error },
                        onSuccess = { state = PersonalDeadlinesView.State.Deadlines(it) }
                )
    }

    fun removeDeadlines() {
        val recordId = (state as? PersonalDeadlinesView.State.Deadlines)?.record?.id ?: return
        state = PersonalDeadlinesView.State.Loading
        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINE_DELETED)
        compositeDisposable addDisposable deadlinesRepository.removeDeadlinesForCourseByRecordId(recordId)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { state = PersonalDeadlinesView.State.Error },
                        onComplete = { state = PersonalDeadlinesView.State.EmptyDeadlines }
                )
    }

    private fun setStateToView(state: PersonalDeadlinesView.State) {
        when (state) {
            is PersonalDeadlinesView.State.Deadlines ->
                view?.setDeadlines(state.record)

            is PersonalDeadlinesView.State.EmptyDeadlines ->
                view?.setDeadlines(null)
        }
    }

    fun onClickCreateDeadlines() {
        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINES_WIDGET_CLICKED,
                Bundle().apply { putLong(Analytic.Deadlines.Params.COURSE, courseId) })
        view?.showLearningRateDialog()
    }

    fun onClickHideDeadlinesBanner() {
        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINES_WIDGET_HIDDEN,
                Bundle().apply { putLong(Analytic.Deadlines.Params.COURSE, courseId) })
    }
}