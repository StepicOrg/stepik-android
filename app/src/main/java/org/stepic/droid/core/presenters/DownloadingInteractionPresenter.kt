package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.DownloadingInteractionView
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.connectivity.NetworkTypeDeterminer
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class DownloadingInteractionPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val userPreferences: UserPreferences,
        private val networkTypeDeterminer: NetworkTypeDeterminer)
    : PresenterBase<DownloadingInteractionView>() {

    private val isHandling = AtomicBoolean(false)

    fun checkOnLoading(position: Int) {
        if (isHandling.compareAndSet(false, true)) {
            threadPoolExecutor.execute {
                try {
                    checkOnLoadingExclusive(position)
                } finally {
                    isHandling.set(false)
                }
            }
        }
    }

    private fun checkOnLoadingExclusive(position: Int) {

    }

}
