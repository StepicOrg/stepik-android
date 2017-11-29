package org.stepic.droid.notifications.badges

import android.content.Context
import android.support.annotation.MainThread
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Scheduler
import io.reactivex.Single
import me.leolin.shortcutbadger.ShortcutBadger
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.notifications.model.NotificationStatuses
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DateTimeHelper.nowUtc
import org.stepic.droid.web.Api
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject

@AppSingleton
class NotificationsBadgesManager
@Inject
constructor(
        private val api: Api,
        private val context: Context,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val firebaseRemoteConfig: FirebaseRemoteConfig,

        @BackgroundScheduler
        private val scheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) {
    private val lock = ReentrantLock()

    fun fetchAndThenSyncCounter() {
        Single.fromCallable { sharedPreferenceHelper.notificationsCount }
                .subscribeOn(scheduler)
                .observeOn(mainScheduler)
                .subscribe { count, _ -> updateCounter(count) }
        syncCounter()
    }

    fun syncCounter() {
        val now = nowUtc()
        api.notificationStatuses
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .subscribe { res, _ ->
                    res.notificationStatuses?.firstOrNull()?.let {
                        setCounter(it, now)
                    }
                }
    }

    fun setCounter(notificationStatuses: NotificationStatuses, timestamp: Long) {
        Single.fromCallable {
            lock.lock()
            try {
                if (sharedPreferenceHelper.notificationsCountTimestamp < timestamp) {
                    sharedPreferenceHelper.setNotificationsCount(notificationStatuses.total, timestamp)
                }
                return@fromCallable sharedPreferenceHelper.notificationsCount
            } finally {
                lock.unlock()
            }
        }
                .subscribeOn(scheduler)
                .observeOn(mainScheduler)
                .subscribe { count, _ -> updateCounter(count) }
    }

    @MainThread
    private fun updateCounter(count: Int) {
        if (firebaseRemoteConfig.getBoolean(RemoteConfig.SHOW_NOTIFICATOINS_BADGES)) {
            ShortcutBadger.applyCount(context, count)
        }
    }

}