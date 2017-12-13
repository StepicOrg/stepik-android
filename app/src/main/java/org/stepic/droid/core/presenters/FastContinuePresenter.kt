package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.stepic.droid.core.FirstCourseProvider
import org.stepic.droid.core.presenters.contracts.FastContinueView
import org.stepic.droid.di.course_list.CourseGeneralScope
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

@CourseGeneralScope
class FastContinuePresenter
@Inject
constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val firstCourseProvider: FirstCourseProvider,
        @MainScheduler
        private val mainScheduler: Scheduler
) : PresenterBase<FastContinueView>() {

    private var disposable: Disposable? = null

    fun onCreated() {
        if (sharedPreferenceHelper.authResponseFromStore != null) {
            view?.onLoading()
            subscribeToFirstCourse()
        } else {
            view?.onAnonymous()
        }
    }

    private fun subscribeToFirstCourse() {
        disposable = firstCourseProvider
                .firstCourse()
                .observeOn(mainScheduler)
                .subscribe {
                    val course = it.value
                    if (course == null) {
                        view?.onEmptyCourse()
                    } else {
                        view?.onShowCourse(course)
                    }
                }
    }

    override fun detachView(view: FastContinueView) {
        disposable?.dispose()
        super.detachView(view)
    }


}
