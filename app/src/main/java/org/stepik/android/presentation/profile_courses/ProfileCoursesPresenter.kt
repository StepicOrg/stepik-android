package org.stepik.android.presentation.profile_courses

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.interactor.ContinueLearningInteractor
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileCoursesPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val profileDataObservable: Observable<ProfileData>,
    private val courseListInteractor: CourseListInteractor,

    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val continueLearningInteractor: ContinueLearningInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileCoursesView>() {
    private var state: ProfileCoursesView.State = ProfileCoursesView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: ProfileCoursesView) {
        super.attachView(view)
        view.setState(state)
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
    
    fun continueCourse(course: Course) {
        analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE)
        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Course.CONTINUE_PRESSED, mapOf(
                AmplitudeAnalytic.Course.Params.COURSE to course.id,
                AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.COURSE_WIDGET
            ))

        if (adaptiveCoursesResolver.isAdaptive(course.id)) {
            view?.showCourse(course, isAdaptive = true)
        } else {
            compositeDisposable += continueLearningInteractor
                .getLastStepForCourse(course)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { view?.showSteps(course, it) },
                    onError = { view?.showCourse(course, isAdaptive = false) }
                )
        }
    }
}