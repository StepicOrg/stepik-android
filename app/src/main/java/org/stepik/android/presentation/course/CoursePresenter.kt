package org.stepik.android.presentation.course

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions.EMPTY_ACTION
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.CourseInteractor
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CoursePresenter
@Inject
constructor(
        private val courseInteractor: CourseInteractor,

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
        state = CourseView.State.Loading
        addDisposable(courseInteractor
                .getCourse(courseId)
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe({
                    state = CourseView.State.CourseLoaded(it)
                }, {
                    state = CourseView.State.NetworkError
                }))
    }

    fun onCourse(course: Course) {
        if (state == CourseView.State.Idle) { // new creation / from arguments / from saved instance state
            state = CourseView.State.CourseLoaded(course)
            courseInteractor.notifyCourse(course)
        }
    }

    fun enrollCourse() {
        val oldState = state
        if (oldState is CourseView.State.CourseLoaded) {
            state = CourseView.State.EnrollmentProgress(oldState.course)
            addDisposable(courseInteractor
                    .enrollCourse(oldState.course.id)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribe(EMPTY_ACTION, Consumer {
                        view?.showEnrollmentError()
                        state = CourseView.State.CourseLoaded(oldState.course)
                    }))
        }
    }

    fun dropCourse() {
        val oldState = state
        if (oldState is CourseView.State.CourseLoaded) {
            state = CourseView.State.EnrollmentProgress(oldState.course)
            addDisposable(courseInteractor
                    .dropCourse(oldState.course.id)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribe(EMPTY_ACTION, Consumer {
                        view?.showEnrollmentError()
                        state = CourseView.State.CourseLoaded(oldState.course)
                    }))
        }
    }

    private fun updateEnrollment(isEnrolled: Boolean) {
        val course = when(val oldState = state) {
            is CourseView.State.CourseLoaded ->
                oldState.course
            is CourseView.State.EnrollmentProgress ->
                oldState.course
            else -> null
        }

        if (course != null) {
            course.enrollment = if (isEnrolled) course.id.toInt() else 0
            state = CourseView.State.CourseLoaded(course)
        }
    }
}