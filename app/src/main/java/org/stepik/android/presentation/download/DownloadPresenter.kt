package org.stepik.android.presentation.download

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.download.interactor.DownloadInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class DownloadPresenter
@Inject
constructor(
    private val downloadInteractor: DownloadInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<DownloadView>() {
    private var state: DownloadView.State = DownloadView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    override fun attachView(view: DownloadView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchDownloadedCourses() {
        if (state != DownloadView.State.Idle) return

        state = DownloadView.State.Loading
        compositeDisposable += downloadInteractor.fetchDownloadCourses()
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = DownloadView.State.DownloadedCoursesLoaded(it) },
                onError = { it.printStackTrace() }
            )
    }
}