package org.stepic.droid.core

import android.app.DownloadManager
import android.os.Build
import android.webkit.CookieManager
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.notifications.badges.NotificationsBadgesLogoutPoster
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.FileUtil
import org.stepic.droid.util.RWLocks
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@AppSingleton
class StepikLogoutManager
@Inject
constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val externalStorageManager: ExternalStorageManager,
        private val systemDownloadManager: DownloadManager,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val databaseFacade: DatabaseFacade,
        private val notificationsBadgesLogoutPoster: NotificationsBadgesLogoutPoster
) {

    fun logout(afterClearData: () -> Unit) {
        threadPoolExecutor.execute {
            removeCookiesCompat()
            val directoryForClean = externalStorageManager.getSelectedStorageLocation().path
            val downloadEntities = databaseFacade.getAllDownloadEntities()
            downloadEntities.forEach {
                it?.downloadId?.let {
                    downloadId ->
                    systemDownloadManager.remove(downloadId)
                }
            }
            FileUtil.cleanDirectory(directoryForClean)
            try {
                RWLocks.ClearEnrollmentsLock.writeLock().lock()
                sharedPreferenceHelper.deleteAuthInfo()
                databaseFacade.dropDatabase()
            } finally {
                RWLocks.ClearEnrollmentsLock.writeLock().unlock()
            }
            mainHandler.post {
                notificationsBadgesLogoutPoster.clearCounter()
                afterClearData.invoke()
            }
        }
    }

    private fun removeCookiesCompat() {
        if (Build.VERSION.SDK_INT < 21) {
            @Suppress("DEPRECATION")
            CookieManager.getInstance().removeAllCookie()
        } else {
            CookieManager.getInstance().removeAllCookies(null)
        }
    }
}
