package org.stepik.android.presentation.download

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.downloads.interactor.DownloadInteractor
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.download.interactor.DownloadsInteractor
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.download.mapper.DownloadItemsStateMapper
import javax.inject.Inject

class DownloadPresenter
@Inject
constructor(
    private val downloadsInteractor: DownloadsInteractor,
    private val downloadItemsStateMapper: DownloadItemsStateMapper,

    private val courseDownloadInteractor: DownloadInteractor<Course>,

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
        compositeDisposable += downloadsInteractor.fetchDownloadItems()
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { state = downloadItemsStateMapper.addDownloadItem(state, it) },
                onError = emptyOnErrorStub
            )
    }

    fun removeCourseDownload(course: Course) {
        compositeDisposable += courseDownloadInteractor
            .removeTask(course)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy()
    }
}