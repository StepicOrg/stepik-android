package org.stepic.droid.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class NotificationBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var databaseFacade: DatabaseFacade

    @Inject
    lateinit var threadPool: ThreadPoolExecutor

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var sharedPreferences: SharedPreferenceHelper

    init {
        App.component().inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == AppConstants.NOTIFICATION_CANCELED) {
            analytic.reportEvent(Analytic.Notification.DISCARD)
            val courseId = intent.extras?.getLong(AppConstants.COURSE_ID_KEY)
            courseId?.let {
                threadPool.execute {
                    databaseFacade.removeAllNotificationsWithCourseId(courseId)
                }
            }
        } else if (action == AppConstants.NOTIFICATION_CANCELED_REMINDER) {
            Timber.d(Analytic.Notification.REMINDER_SWIPE_TO_CANCEL)
            analytic.reportEvent(Analytic.Notification.REMINDER_SWIPE_TO_CANCEL)
        } else if (action == AppConstants.NOTIFICATION_CANCELED_STREAK) {
            analytic.reportEvent(Analytic.Notification.STREAK_SWIPE_TO_CANCEL)
            //continue send notification about streaks
            threadPool.execute {
                sharedPreferences.resetNumberOfStreakNotifications()
            }
        }
    }
}