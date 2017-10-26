package org.stepic.droid.core.downloadingProgress

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import timber.log.Timber
import javax.inject.Inject

class DownloadingPresenter
@Inject
constructor(
        private val progressWatcher: ProgressWatcher,
        @BackgroundScheduler
        private val scheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler) : PresenterBase<DownloadingView>() {

    private val map = hashMapOf<Long, Disposable>()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun onStateChanged(id: Long, isLoading: Boolean) {
        if (isLoading) {
            listenLoadingProgress(id)
        } else {
            stopListenProgress(id)
        }
    }

    override fun detachView(view: DownloadingView) {
        super.detachView(view)
        compositeDisposable.dispose()
        map.clear()
    }

    private fun listenLoadingProgress(id: Long) {
        if (map.containsKey(id)) {
            //we already listen
            return
        }
        val disposable = progressWatcher
                .watch(id)
                .subscribeOn(scheduler)
                .observeOn(mainScheduler)
                .subscribe { progress -> view?.onNewProgressValue(id, progress) }
        map.put(id, disposable)
        compositeDisposable.add(disposable)
    }

    private fun stopListenProgress(id: Long) {
        map.remove(id)?.let { compositeDisposable.remove(it) }
    }

}
