package org.stepic.droid.core.presenters

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.contracts.FastContinueView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.course_list.model.UserCoursesLoaded
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course_list.UserCoursesLoadedBus
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
    private val userCoursesLoadedObservable: Observable<UserCoursesLoaded>
) : PresenterBase<FastContinueView>() {

    private var disposable: Disposable? = null
    private var course: Course? = null

    fun onCreated() {
        if (sharedPreferenceHelper.authResponseFromStore != null) {
            if (course == null) {
                view?.onLoading()
                subscribeToFirstCourse()
            } else {
                view?.onShowCourse(course as Course)
            }
        } else {
            view?.onAnonymous()
        }
    }

    private fun subscribeToFirstCourse() {
        disposable = userCoursesLoadedObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    if (it is UserCoursesLoaded.FirstCourse) {
                        course = it.course
                        view?.onShowCourse(it.course)
                    } else {
                        view?.onEmptyCourse()
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    override fun detachView(view: FastContinueView) {
        disposable?.dispose()
        super.detachView(view)
    }


}
