package org.stepik.android.presentation.profile_courses

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.injection.profile.UserId
import javax.inject.Inject

class ProfileCoursesPresenter
@Inject
constructor(
    @UserId
    private val userId: Long,
    private val courseListInteractor: CourseListInteractor,

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

    fun fetchUserActivities(forceUpdate: Boolean = false) {
        if (state == ProfileCoursesView.State.Idle || (forceUpdate && state is ProfileCoursesView.State.Error)) {
            state = ProfileCoursesView.State.SilentLoading
            compositeDisposable += courseListInteractor
                .getCourseList(
                    CourseListQuery(
                        teacher = userId,
                        order = CourseListQuery.ORDER_POPULARITY_DESC
                    )
                )
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
}