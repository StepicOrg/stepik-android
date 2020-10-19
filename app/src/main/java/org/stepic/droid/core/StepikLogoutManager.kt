package org.stepic.droid.core

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.notifications.badges.NotificationsBadgesLogoutPoster
import org.stepic.droid.persistence.downloads.interactor.RemovalDownloadsInteractor
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.RWLocks
import org.stepik.android.cache.base.database.AnalyticDatabase
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@AppSingleton
class StepikLogoutManager
@Inject
constructor(
    private val threadPoolExecutor: ThreadPoolExecutor,
    private val mainHandler: MainHandler,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val databaseFacade: DatabaseFacade,
    private val analyticDatabase: AnalyticDatabase,
    private val notificationsBadgesLogoutPoster: NotificationsBadgesLogoutPoster,
    private val removalDownloadsInteractor: RemovalDownloadsInteractor
) {

    fun logout(afterClearData: () -> Unit) {
        threadPoolExecutor.execute {
            removalDownloadsInteractor.removeAllDownloads().blockingAwait()
            try {
                RWLocks.ClearEnrollmentsLock.writeLock().lock()
                sharedPreferenceHelper.deleteAuthInfo()
                databaseFacade.dropDatabase()
                analyticDatabase.clearAllTables()
            } finally {
                RWLocks.ClearEnrollmentsLock.writeLock().unlock()
            }
            mainHandler.post {
                notificationsBadgesLogoutPoster.clearCounter()
                afterClearData.invoke()
            }
        }
    }
}
