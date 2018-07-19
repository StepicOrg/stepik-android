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
import org.stepik.android.model.Course
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.features.deadlines.model.LearningRate
import org.stepic.droid.features.deadlines.presenters.contracts.PersonalDeadlinesView
import org.stepic.droid.features.deadlines.repository.DeadlinesRepository
import org.stepik.android.model.Section
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

    private var shouldShowBanner = false

    fun fetchDeadlinesForCourse(course: Course?, sections: List<Section>) {
        if (course == null) {
            state = PersonalDeadlinesView.State.NoDeadlinesNeeded
            return
        }

        courseId = course.id

        when {
            sections.any { it.softDeadline != null || it.hardDeadline != null } -> // there are teacher's deadlines
                state = PersonalDeadlinesView.State.NoDeadlinesNeeded

            state == PersonalDeadlinesView.State.Idle -> {
                state = PersonalDeadlinesView.State.BackgroundLoading
                compositeDisposable addDisposable deadlinesRepository.getDeadlinesForCourse(course.id)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                                onError = { onError(PersonalDeadlinesView.State.Idle) },
                                onComplete = { state = PersonalDeadlinesView.State.EmptyDeadlines },
                                onSuccess = { state = PersonalDeadlinesView.State.Deadlines(it) }
                        )

                compositeDisposable addDisposable deadlinesRepository.shouldShowDeadlinesBannerForCourse(courseId)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe { shouldShow ->
                            shouldShowBanner = shouldShow
                            setStateToView(state)
                        }
            }

            else -> setStateToView(state)
        }
    }

    fun createDeadlinesForCourse(course: Course?, learningRate: LearningRate) {
        if (course == null || state != PersonalDeadlinesView.State.EmptyDeadlines) return

        val oldState = state
        state = PersonalDeadlinesView.State.BlockingLoading

        compositeDisposable addDisposable deadlinesResolver.calculateDeadlinesForCourse(course.id, learningRate)
                .flatMap { deadlinesRepository.createDeadlinesForCourse(it) }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { onError(oldState) },
                        onSuccess = { state = PersonalDeadlinesView.State.Deadlines(it) }
                )
    }

    fun updateDeadlines(deadlines: List<Deadline>) {
        val record = (state as? PersonalDeadlinesView.State.Deadlines)?.record ?: return
        val newRecord = record.copy(data = DeadlinesWrapper(record.data.course, deadlines))

        val oldState = state
        state = PersonalDeadlinesView.State.BlockingLoading

        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINE_TIME_SAVED)
        compositeDisposable addDisposable deadlinesRepository.updateDeadlinesForCourse(newRecord)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { onError(oldState) },
                        onSuccess = { state = PersonalDeadlinesView.State.Deadlines(it) }
                )
    }

    fun removeDeadlines() {
        val recordId = (state as? PersonalDeadlinesView.State.Deadlines)?.record?.id ?: return

        val oldState = state
        state = PersonalDeadlinesView.State.BlockingLoading

        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINE_DELETED)
        compositeDisposable addDisposable deadlinesRepository.removeDeadlinesForCourseByRecordId(recordId)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { onError(oldState) },
                        onComplete = { state = PersonalDeadlinesView.State.EmptyDeadlines }
                )
    }

    private fun onError(oldState: PersonalDeadlinesView.State) {
        state = PersonalDeadlinesView.State.Error
        state = oldState
    }

    private fun setStateToView(state: PersonalDeadlinesView.State) {
        when (state) {
            is PersonalDeadlinesView.State.Deadlines -> {
                shouldShowBanner = false
                view?.setDeadlines(state.record)
                view?.setDeadlinesControls(true, false)
            }

            is PersonalDeadlinesView.State.EmptyDeadlines -> {
                view?.setDeadlines(null)
                view?.setDeadlinesControls(true, shouldShowBanner)
            }

            is PersonalDeadlinesView.State.NoDeadlinesNeeded,
            is PersonalDeadlinesView.State.BackgroundLoading -> {
                view?.setDeadlines(null)
                view?.setDeadlinesControls(false, false)
            }

            is PersonalDeadlinesView.State.BlockingLoading ->
                view?.showLoadingDialog()

            is PersonalDeadlinesView.State.Error ->
                view?.showPersonalDeadlinesError()
        }
    }

    fun onClickCreateDeadlines(fromWidget: Boolean = false) {
        if (fromWidget) {
            analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINES_WIDGET_CLICKED,
                    Bundle().apply { putLong(Analytic.Deadlines.Params.COURSE, courseId) })
        }
        view?.showLearningRateDialog()
    }

    fun onClickHideDeadlinesBanner() {
        analytic.reportEvent(Analytic.Deadlines.PERSONAL_DEADLINES_WIDGET_HIDDEN,
                Bundle().apply { putLong(Analytic.Deadlines.Params.COURSE, courseId) })
        shouldShowBanner = false
        setStateToView(state)

        compositeDisposable addDisposable deadlinesRepository.hideDeadlinesBannerForCourse(courseId)
                .observeOn(backgroundScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe()
    }
}