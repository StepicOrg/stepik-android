package org.stepic.droid.core

import android.app.DownloadManager
import android.os.Build
import android.webkit.CookieManager
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.FileUtil
import org.stepic.droid.util.RWLocks
import java.util.concurrent.ThreadPoolExecutor

class StepikLogoutManager(private val threadPoolExecutor: ThreadPoolExecutor,
                          private val mainHandler: MainHandler,
                          private val userPreferences: UserPreferences,
                          private val systemDownloadManager: DownloadManager,
                          private val sharedPreferenceHelper: SharedPreferenceHelper,
                          private val databaseFacade: DatabaseFacade) {

    fun logout(afterClearData: () -> Unit) {
        threadPoolExecutor.execute {
            removeCookiesCompat()
            val directoryForClean = userPreferences.userDownloadFolder
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
                afterClearData.invoke()
            }
        }
    }

    private fun removeCookiesCompat() {
        CookieManager.getInstance()
        if (Build.VERSION.SDK_INT < 21) {
            CookieManager.getInstance().removeAllCookie()
        } else {
            mainHandler.post {
                CookieManager.getInstance().removeAllCookies() {}
            }
        }
    }
}
