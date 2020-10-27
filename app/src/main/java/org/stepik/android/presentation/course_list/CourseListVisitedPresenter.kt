package org.stepik.android.presentation.course_list

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_list.interactor.CourseListVisitedInteractor
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class CourseListVisitedPresenter
@Inject
constructor(
    private val courseListStateMapper: CourseListStateMapper,
    private val courseListVisitedInteractor: CourseListVisitedInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,
    @UserCoursesOperationBus
    private val userCourseOperationObservable: Observable<UserCourse>,
    viewContainer: PresenterViewContainer<CourseListView>,
    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<CourseListView>(viewContainer), CourseContinuePresenterDelegate by continueCoursePresenterDelegate {

    override val delegates: List<PresenterDelegate<in CourseListView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: CourseListView.State = CourseListView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        subscribeForEnrollmentUpdates()
        subscribeForUserCourseOperationUpdates()
    }

    override fun attachView(view: CourseListView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchCourses() {
        if (state != CourseListView.State.Idle) return

        val oldState = state

        state = CourseListView.State.Loading

        compositeDisposable += courseListVisitedInteractor
            .getVisitedCourseListItems()
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { items ->
                    state = if (items.isNotEmpty()) {
                        CourseListView.State.Content(
                            courseListDataItems = items,
                            courseListItems = items
                        )
                    } else {
                        CourseListView.State.Empty
                    }
                },
                onError = {
                    when (oldState) {
                        is CourseListView.State.Content -> {
                            state = oldState
                            view?.showNetworkError()
                        }
                        else ->
                            state = CourseListView.State.NetworkError
                    }
                }
            )
    }

    /**
     * Enrollment updates
     */
    private fun subscribeForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { enrolledCourse ->
                    state = courseListStateMapper.mapToEnrollmentUpdateState(state, enrolledCourse)
                    fetchForEnrollmentUpdate(enrolledCourse)
                },
                onError = emptyOnErrorStub
            )
    }

    private fun fetchForEnrollmentUpdate(course: Course) {
        compositeDisposable += courseListVisitedInteractor
            .getCourseListItems(listOf(course.id), courseViewSource = CourseViewSource.Visited, sourceTypeComposition = SourceTypeComposition.CACHE)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courses ->
                    state = courseListStateMapper.mapToEnrollmentUpdateState(state, courses.first())
                },
                onError = emptyOnErrorStub
            )
    }

    /**
     * UserCourse updates
     */
    private fun subscribeForUserCourseOperationUpdates() {
        compositeDisposable += userCourseOperationObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { userCourse ->
                    state = courseListStateMapper.mapToUserCourseUpdate(state, userCourse)
                },
                onError = emptyOnErrorStub
            )
    }
}