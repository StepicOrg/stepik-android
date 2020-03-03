package org.stepik.android.presentation.profile_courses

import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.delegate.ContinueCoursePresenterDelegate
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import javax.inject.Inject

class ProfileCoursesPresenter
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val courseListInteractor: CourseListInteractor,

    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,

    viewContainer: PresenterViewContainer<ProfileCoursesView>,

    private val continueCoursePresenterDelegate: ContinueCoursePresenterDelegate,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileCoursesView>(viewContainer), ContinueCoursePresenterDelegate by continueCoursePresenterDelegate {
    companion object {
        private const val KEY_COURSES = "courses"
    }

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
                state = ProfileCoursesView.State.SilentLoading
                compositeDisposable += courseListInteractor
                    .getSavedCourses(courseIds)
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
            state = ProfileCoursesView.State.SilentLoading
            compositeDisposable += profileDataObservable
                .firstElement()
                .flatMapSingleElement { profileData ->
                    // TODO Pagination
                    courseListInteractor
                        .getCourses(
                            CourseListQuery(
                                teacher = profileData.user.id,
                                order = CourseListQuery.ORDER_POPULARITY_DESC
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
                onNext = { course ->
                    val oldState = state
                    if (oldState is ProfileCoursesView.State.Content) {
                        state = ProfileCoursesView.State.Content(oldState.courses.map { if (it.id == course.id) course else it })
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val courses = (state as? ProfileCoursesView.State.Content)
            ?.courses
            ?: return

        outState.putLongArray(KEY_COURSES, courses.mapToLongArray(Course::id))
        super.onSaveInstanceState(outState)
    }
}