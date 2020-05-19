package org.stepik.android.presentation.profile_courses

import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.mapPaged
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import org.stepik.android.view.injection.profile.UserId
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class ProfileCoursesPresenter
@Inject
constructor(
    @UserId
    private val userId: Long,
    private val profileDataObservable: Observable<ProfileData>,
    private val courseListInteractor: CourseListInteractor,

    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,

    viewContainer: PresenterViewContainer<ProfileCoursesView>,

    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileCoursesView>(viewContainer), CourseContinuePresenterDelegate by continueCoursePresenterDelegate {
    companion object {
        private const val KEY_COURSES = "courses"
        private const val KEY_USER = "user"
    }

    override val delegates: List<PresenterDelegate<in ProfileCoursesView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: ProfileCoursesView.State = ProfileCoursesView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private var isBlockingLoading: Boolean = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    init {
        subscriberForEnrollmentUpdates()
    }

    override fun attachView(view: ProfileCoursesView) {
        super.attachView(view)
        view.setBlockingLoading(isBlockingLoading)
        view.setState(state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val courseIds = savedInstanceState.getLongArray(KEY_COURSES)
        if (courseIds != null) {
            if (state == ProfileCoursesView.State.Idle) {
                state = ProfileCoursesView.State.Loading
                compositeDisposable += courseListInteractor // TODO Cache data source?
                    .getCourseListItems(*courseIds, courseViewSource = CourseViewSource.Query(CourseListQuery(teacher = userId, order = CourseListQuery.Order.POPULARITY_DESC)))
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { state = ProfileCoursesView.State.Content(it) },
                        onError = { state = ProfileCoursesView.State.Idle; fetchCourses() }
                    )
            }
        }
    }

    fun fetchCourses(forceUpdate: Boolean = false) {
        if (state == ProfileCoursesView.State.Idle || (forceUpdate && state is ProfileCoursesView.State.Error)) {
            state = ProfileCoursesView.State.Loading
            compositeDisposable += profileDataObservable
                .firstElement()
                .flatMapSingleElement { profileData ->
                    // TODO Pagination
                    courseListInteractor
                        .getCourseListItems(
                            CourseListQuery(
                                teacher = profileData.user.id,
                                order = CourseListQuery.Order.POPULARITY_DESC
                            )
                        )
                }
                .filter { it.isNotEmpty() }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { state = ProfileCoursesView.State.Content(it) },
                    onComplete = { state = ProfileCoursesView.State.Empty },
                    onError = { state = ProfileCoursesView.State.Error }
                )
        }
    }

    private fun subscriberForEnrollmentUpdates() {
        compositeDisposable += enrollmentUpdatesObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { enrolledCourse ->
                    val oldState = state
                    if (oldState is ProfileCoursesView.State.Content) {
                        state = ProfileCoursesView.State.Content(
                            oldState.courseListDataItems.mapPaged {
                                if (it.course.id == enrolledCourse.id) it.copy(course = enrolledCourse) else it
                            }
                        )
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val courseListDataItems = (state as? ProfileCoursesView.State.Content)
            ?.courseListDataItems
            ?: return

        outState.putLongArray(KEY_COURSES, courseListDataItems.mapToLongArray(CourseListItem.Data::id))
        super.onSaveInstanceState(outState)
    }
}