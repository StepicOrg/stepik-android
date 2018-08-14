package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.contracts.DownloadsView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.persistence.repository.DownloadsRepository
import javax.inject.Inject

class DownloadsPresenter
@Inject
constructor(
        private val downloadsRepository: DownloadsRepository,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
): PresenterBase<DownloadsView>() {
    private var disposable: Disposable? = null

    private fun subscribeForDownloads() {
        disposable?.dispose()
        disposable = downloadsRepository.getDownloads()
                .observeOn(backgroundScheduler)
                .subscribeOn(mainScheduler)
                .subscribeBy(onError = { subscribeForDownloads() }) {
                    when(it.status) {
                        is DownloadProgress.Status.Cached ->
                            view?.addCompletedDonwload(it)

                        is DownloadProgress.Status.NotCached ->
                            view?.removeDownload(it)

                        else ->
                            view?.addActiveDownload(it)
                    }
                }
    }

    override fun attachView(view: DownloadsView) {
        super.attachView(view)
        subscribeForDownloads()
    }

    override fun detachView(view: DownloadsView) {
        disposable?.dispose()
        super.detachView(view)
    }
}