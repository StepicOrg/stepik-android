package org.stepik.android.presentation.fast_continue

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.course_list.model.UserCoursesLoaded
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import org.stepik.android.view.injection.course_list.UserCoursesLoadedBus
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class FastContinuePresenter
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    @MainScheduler
    private val mainScheduler: Scheduler,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @UserCoursesLoadedBus
    private val userCoursesLoadedObservable: Observable<UserCoursesLoaded>,

    viewContainer: PresenterViewContainer<FastContinueView>,

    continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<FastContinueView>(viewContainer), CourseContinuePresenterDelegate by continueCoursePresenterDelegate {

    override val delegates: List<PresenterDelegate<in FastContinueView>> =
        listOf(continueCoursePresenterDelegate)

    private var state: FastContinueView.State = FastContinueView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        subscribeToFirstCourse()
        onCreated()
    }

    override fun attachView(view: FastContinueView) {
        super.attachView(view)
        view.setState(state)
    }

    private fun onCreated() {
        state = if (sharedPreferenceHelper.authResponseFromStore != null) {
            FastContinueView.State.Loading
        } else {
            FastContinueView.State.Anonymous
        }
    }

    private fun subscribeToFirstCourse() {
        compositeDisposable += userCoursesLoadedObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    state = if (it is UserCoursesLoaded.FirstCourse) {
                        FastContinueView.State.Content(courseListItem = it.courseListItem)
                    } else {
                        FastContinueView.State.Empty
                    }
                },
                onError = emptyOnErrorStub
            )
    }
}