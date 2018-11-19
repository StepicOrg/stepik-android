package org.stepik.android.presentation.course

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions.EMPTY_ACTION
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.CourseEnrollmentInteractor
import org.stepik.android.domain.course.interactor.CourseInteractor
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CoursePresenter
@Inject
constructor(
        private val courseInteractor: CourseInteractor,
        private val courseEnrollmentInteractor: CourseEnrollmentInteractor,

        enrollmentObservable: Observable<Boolean>,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) : PresenterBase<CourseView>() {
    private var state: CourseView.State = CourseView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        addDisposable(enrollmentObservable
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe(::updateEnrollment))
    }

    override fun attachView(view: CourseView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onCourseId(courseId: Long) {
        observeCourseData(courseInteractor.getCourseHeaderData(courseId))
    }

    fun onCourse(course: Course) {
        observeCourseData(courseInteractor.getCourseHeaderData(course))
    }

    private fun observeCourseData(courseDataObservable: Maybe<CourseHeaderData>) {
        state = CourseView.State.Loading
        addDisposable(courseDataObservable
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = { state = CourseView.State.EmptyCourse },
                onSuccess  = { state = CourseView.State.CourseLoaded(it) },
                onError    = { state = CourseView.State.NetworkError }
            ))
    }

    fun enrollCourse() {
        val oldState = state
        if (oldState is CourseView.State.CourseLoaded) {
            state = CourseView.State.EnrollmentProgress(oldState.courseHeaderData)
            addDisposable(courseEnrollmentInteractor
                    .enrollCourse(oldState.courseHeaderData.courseId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribe(EMPTY_ACTION, Consumer {
                        view?.showEnrollmentError()
                        state = CourseView.State.CourseLoaded(oldState.courseHeaderData)
                    }))
        }
    }

    fun dropCourse() {
        val oldState = state
        if (oldState is CourseView.State.CourseLoaded) {
            state = CourseView.State.EnrollmentProgress(oldState.courseHeaderData)
            addDisposable(courseEnrollmentInteractor
                    .dropCourse(oldState.courseHeaderData.courseId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribe(EMPTY_ACTION, Consumer {
                        view?.showEnrollmentError()
                        state = CourseView.State.CourseLoaded(oldState.courseHeaderData)
                    }))
        }
    }

    private fun updateEnrollment(isEnrolled: Boolean) {
        val courseHeaderData = when(val oldState = state) {
            is CourseView.State.CourseLoaded ->
                oldState.courseHeaderData
            is CourseView.State.EnrollmentProgress ->
                oldState.courseHeaderData
            else -> null
        }

        if (courseHeaderData != null) {
            state = CourseView.State.CourseLoaded(courseHeaderData.copy(isEnrolled = isEnrolled))
        }
    }
}