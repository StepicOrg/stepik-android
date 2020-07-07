package org.stepic.droid.notifications.badges

import android.content.Context
import androidx.annotation.MainThread
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import me.leolin.shortcutbadger.ShortcutBadger
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.notification.repository.NotificationRepository
import ru.nobird.android.domain.rx.emptyOnErrorStub
import javax.inject.Inject

@AppSingleton
class NotificationsBadgesManager
@Inject
constructor(
    private val context: Context,
    private val notificationRepository: NotificationRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val listenerContainer: ListenerContainer<NotificationsBadgesListener>,
    @BackgroundScheduler
    private val scheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) {
    fun fetchAndThenSyncCounter() {
        Single
            .fromCallable { sharedPreferenceHelper.notificationsCount }
            .subscribeOn(scheduler)
            .observeOn(mainScheduler)
            .subscribe { count, _ ->
                count?.let { updateCounter(it) }
                syncCounter()
            }
    }

    fun syncCounter() {
        notificationRepository
            .getNotificationStatuses()
            .map {
                it.firstOrNull()?.total ?: 0
            }
            .doOnSuccess {
                sharedPreferenceHelper.notificationsCount = it
            }
            .subscribeOn(scheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    updateCounter(it)
                },
                onError = emptyOnErrorStub
            )
    }

    @MainThread
    private fun updateCounter(count: Int) {
        if (firebaseRemoteConfig[RemoteConfig.SHOW_NOTIFICATIONS_BADGES].asBoolean() && count != 0) {
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