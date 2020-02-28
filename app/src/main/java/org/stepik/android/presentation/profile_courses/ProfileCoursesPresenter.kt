package org.stepik.android.presentation.profile_courses

import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.course.interactor.ContinueLearningInteractor
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import timber.log.Timber
import javax.inject.Inject

class ProfileCoursesPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val profileDataObservable: Observable<ProfileData>,
    private val courseListInteractor: CourseListInteractor,

    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val continueLearningInteractor: ContinueLearningInteractor,

    private val stateContainer: DefaultStateContainer<ProfileCoursesView.State, ProfileCoursesView>,

    private val continueCourseAction: ContinueCourseAction,

    @EnrollmentCourseUpdates
    private val enrollmentUpdatesObservable: Observable<Course>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileCoursesView>(stateContainer), ContinueCourseAction by continueCourseAction {
    companion object {
        private const val KEY_COURSES = "courses"
    }

    private var state by stateContainer

    private var isBlockingLoading: Boolean = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    init {
        Timber.d(stateContainer.toString())
        subscriberForEnrollmentUpdates()
    }

    override fun attachView(view: ProfileCoursesView) {
        super.attachView(view)
        view.setBlockingLoading(isBlockingLoading)
        view.setState(state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val courseIds = savedInstanceState.getLongArray(KEY_COURSES)
        if (courseIds != null) {
            if (state == ProfileCoursesView.State.Idle) {
                state = ProfileCoursesView.State.SilentLoading
                compositeDisposable += courseListInteractor
                    .getCourseList(courseIds)
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
                    courseListInteractor
                        .getCourseList(
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

//    fun continueCourse(course: Course) {
//        analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE)
//        analytic.reportAmplitudeEvent(
//            AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
//                AmplitudeAnalytic.Course.Params.COURSE to course.id,
//                AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.COURSE_WIDGET
//            ))
//
//        if (adaptiveCoursesResolver.isAdaptive(course.id)) {
//            view?.showCourse(course, isAdaptive = true)
//        } else {
//            isBlockingLoading = true
//            compositeDisposable += continueLearningInteractor
//                .getLastStepForCourse(course)
//                .subscribeOn(backgroundScheduler)
//                .observeOn(mainScheduler)
//                .doFinally { isBlockingLoading = false }
//                .subscribeBy(
//                    onSuccess = { view?.showSteps(course, it) },
//                    onError = { view?.showCourse(course, isAdaptive = false) }
//                )
//        }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        val courses = (state as? ProfileCoursesView.State.Content)
            ?.courses
            ?: return

        outState.putLongArray(KEY_COURSES, courses.mapToLongArray(Course::id))
    }
}