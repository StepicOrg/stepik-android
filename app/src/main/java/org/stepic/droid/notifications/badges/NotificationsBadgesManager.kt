package org.stepic.droid.notifications.badges

import android.content.Context
import androidx.annotation.MainThread
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Scheduler
import io.reactivex.Single
import me.leolin.shortcutbadger.ShortcutBadger
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.mapNotNull
import org.stepik.android.data.notification.source.NotificationRemoteDataSource
import javax.inject.Inject

@AppSingleton
class NotificationsBadgesManager
@Inject
constructor(
    private val context: Context,
    private val notificationRemoteDataSrouce: NotificationRemoteDataSource,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val listenerContainer: ListenerContainer<NotificationsBadgesListener>,
    @BackgroundScheduler
        private val scheduler: Scheduler,
    @MainScheduler
        private val mainScheduler: Scheduler
) {
    fun fetchAndThenSyncCounter() {
        Single.fromCallable { sharedPreferenceHelper.notificationsCount }
                .subscribeOn(scheduler)
                .observeOn(mainScheduler)
                .subscribe { count, _ ->
                    count?.let { updateCounter(it) }
                    syncCounter()
                }
    }

    fun syncCounter() {
        notificationRemoteDataSrouce.getNotificationStatuses()
                .mapNotNull {
                    it.notificationStatuses?.firstOrNull()?.total
                }
                .map {
                    sharedPreferenceHelper.notificationsCount = it
                    return@map it
                }
                .subscribeOn(scheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    updateCounter(it)
                }, {})
    }

    @MainThread
    private fun updateCounter(count: Int) {
        if (firebaseRemoteConfig.getBoolean(RemoteConfig.SHOW_NOTIFICATIONS_BADGES) && count != 0) {
            ShortcutBadger.applyCount(context, count)
            listenerContainer.asIterable().forEach {
                it.onBadgeCountChanged(count)
            }
        } else {
            ShortcutBadger.applyCount(context, 0)
            listenerContainer.asIterable().forEach(NotificationsBadgesListener::onBadgeShouldBeHidden)
        }
    }

}