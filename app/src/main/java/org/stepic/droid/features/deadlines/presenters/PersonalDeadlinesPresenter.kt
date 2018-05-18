package org.stepic.droid.features.deadlines.presenters

import android.util.Log
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.features.deadlines.util.DeadlinesResolver
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.Course
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.features.deadlines.presenters.contracts.PersonalDeadlinesView
import org.stepic.droid.util.addDisposable
import org.stepic.droid.web.Api
import org.stepic.droid.web.storage.model.StorageRecord
import javax.inject.Inject

class PersonalDeadlinesPresenter
@Inject
constructor(
        api: Api,

        private val deadlinesResolver: DeadlinesResolver,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
): PresenterBase<PersonalDeadlinesView>() {
    private val deadlinesRepository = api.provideDeadlineRepository()

    private val compositeDisposable = CompositeDisposable()

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

    fun createDeadlinesForCourse(course: Course?, hoursPerWeek: Long) {
        if (course == null || state != PersonalDeadlinesView.State.EmptyDeadlines) return
        state = PersonalDeadlinesView.State.Loading
        compositeDisposable addDisposable deadlinesResolver.calculateDeadlinesForCourse(course.courseId, hoursPerWeek)
                .flatMap { deadlinesRepository.createDeadlinesForCourse(it) }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { state = PersonalDeadlinesView.State.Error },
                        onSuccess = { state = PersonalDeadlinesView.State.Deadlines(it) }
                )
    }

    fun updateDeadlines(record: StorageRecord<DeadlinesWrapper>) {
        state = PersonalDeadlinesView.State.Loading
        compositeDisposable addDisposable deadlinesRepository.updateDeadlinesForCourse(record)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { state = PersonalDeadlinesView.State.Error },
                        onSuccess = { state = PersonalDeadlinesView.State.Deadlines(it) }
                )
    }

    fun removeDeadlines(record: StorageRecord<DeadlinesWrapper>) {
        state = PersonalDeadlinesView.State.Loading
        compositeDisposable addDisposable deadlinesRepository.removeDeadlinesForCourseByRecordId(record.id ?: 0)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { state = PersonalDeadlinesView.State.Error },
                        onComplete = { state = PersonalDeadlinesView.State.EmptyDeadlines }
                )
    }

    private fun setStateToView(state: PersonalDeadlinesView.State) {
        Log.d(javaClass.canonicalName, state.toString())
    }

}