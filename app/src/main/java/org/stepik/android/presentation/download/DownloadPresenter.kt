package org.stepik.android.presentation.download

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.download.interactor.DownloadInteractor
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.download.mapper.DownloadItemsStateMapper
import javax.inject.Inject

class DownloadPresenter
@Inject
constructor(
    private val downloadInteractor: DownloadInteractor,
    private val downloadItemsStateMapper: DownloadItemsStateMapper,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<DownloadView>() {
    private var state: DownloadView.State = DownloadView.State.Empty
        set(value) {
            field = value
            view?.setState(state)
        }

    override fun attachView(view: DownloadView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchDownloadedCourses() {
        compositeDisposable += downloadInteractor.fetchDownloadItems()
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = {
                    val stateParameter = state as? DownloadView.State.DownloadedCoursesLoaded ?: DownloadView.State.DownloadedCoursesLoaded(emptyList())
                    state = downloadItemsStateMapper.addDownloadItem(stateParameter, it)
                },
                onError = emptyOnErrorStub
            )
    }
}