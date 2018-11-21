package org.stepik.android.presentation.course_info

import android.os.Bundle
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_info.interactor.CourseInfoInteractor
import org.stepik.android.domain.course_info.model.CourseInfoData
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CourseInfoPresenter
@Inject
constructor(
    private val courseInfoInteractor: CourseInfoInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseInfoView>() {
    companion object {
        private const val KEY_COURSE_INFO_DATA = "course_info_data"
    }

    private var state: CourseInfoView.State = CourseInfoView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        fetchCourseInfo()
    }

    override fun attachView(view: CourseInfoView) {
        super.attachView(view)
        view.setState(state)
    }

    private fun fetchCourseInfo() {
        state = CourseInfoView.State.Loading
        compositeDisposable += courseInfoInteractor
            .getCourseInfoData()
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onError = { state = CourseInfoView.State.NetworkError },
                onNext  = { state = CourseInfoView.State.CourseInfoLoaded(it) }
            )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (state != CourseInfoView.State.Idle) return
        val data = savedInstanceState.getParcelable(KEY_COURSE_INFO_DATA)
                as? CourseInfoData ?: return
        state = CourseInfoView.State.CourseInfoLoaded(data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_COURSE_INFO_DATA, (state as? CourseInfoView.State.CourseInfoLoaded)?.courseInfoData)
    }
}