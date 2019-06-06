package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.contracts.DownloadsView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.downloads.interactor.RemovalDownloadsInteractor
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.persistence.repository.DownloadsRepository
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.addDisposable
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class DownloadsPresenter
@Inject
constructor(
        private val downloadsRepository: DownloadsRepository,

        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val removalDownloadsInteractor: RemovalDownloadsInteractor,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
): PresenterBase<DownloadsView>() {
    private var disposable: Disposable? = null

    private val compositeDisposable = CompositeDisposable()

    private fun subscribeForDownloads() {
        disposable?.dispose()
        disposable = downloadsRepository.getDownloads()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(onError = { subscribeForDownloads() }) {
                    when(it.status) {
                        is DownloadProgress.Status.Cached ->
                            view?.addCompletedDownload(it)

                        is DownloadProgress.Status.NotCached ->
                            view?.removeDownload(it)

                        else ->
                            view?.addActiveDownload(it)
                    }
                    view?.invalidateEmptyDownloads()
                }
    }

    fun showVideo(downloadItem: DownloadItem) {
        view?.showVideo(VideoPlayerMediaData(
            thumbnail   = downloadItem.video.thumbnail,
            title       = downloadItem.title,
            cachedVideo = downloadItem.video
        ))
    }

    fun onCancelAllDownloadsClicked() {
        view?.askToCancelAllVideos()
    }

    fun onRemoveAllDownloadsClicked() {
        view?.askToRemoveAllCachedVideos()
    }

    fun removeDownloads(downloads: List<DownloadItem>) {
        view?.showLoading()
        compositeDisposable addDisposable removalDownloadsInteractor
                .removeDownloads(downloads)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(onError = {
                    view?.hideLoading()
                    view?.onCantRemoveVideo()
                }) {
                    view?.hideLoading()
                }
    }

    override fun attachView(view: DownloadsView) {
        super.attachView(view)
        if (sharedPreferenceHelper.authResponseFromStore == null) {
            view.showEmptyAuth()
        } else {
            view.invalidateEmptyDownloads()
            subscribeForDownloads()
        }
    }

    override fun detachView(view: DownloadsView) {
        disposable?.dispose()
        super.detachView(view)
    }
}