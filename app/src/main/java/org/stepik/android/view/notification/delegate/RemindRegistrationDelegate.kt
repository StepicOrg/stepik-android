package org.stepik.android.view.notification.delegate

import android.content.Context
import android.content.Intent
import android.support.v4.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.activities.SplashActivity
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotifManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import javax.inject.Inject

class RemindRegistrationDelegate
@Inject constructor(
    val context: Context,
    val sharedPreferenceHelper: SharedPreferenceHelper,
    val notificationHelper: NotificationHelper,
    stepikNotifManager: StepikNotifManager
) : NotificationDelegate("show_registration_notification", stepikNotifManager) {

    companion object {
        private const val REGISTRATION_REMIND_NOTIFICATION_ID = 5L
    }

    override fun onNeedShowNotification() {
        if (sharedPreferenceHelper.isEverLogged) return

        val intent = Intent(context, SplashActivity::class.java)
        val taskBuilder = TaskStackBuilder
                .create(context)
                .addNextIntent(intent)

        val title = context.getString(R.string.stepik_free_courses_title)
        val remindMessage = context.getString(R.string.registration_remind_message)
        val notification = notificationHelper.makeSimpleNotificationBuilder(
                stepikNotification = null,
                justText = remindMessage,
                taskBuilder = taskBuilder,
                title = title,
                id = REGISTRATION_REMIND_NOTIFICATION_ID)

        showNotification(REGISTRATION_REMIND_NOTIFICATION_ID, notification.build())
        scheduleNotification()
    }

    private fun scheduleNotification() {
        if (sharedPreferenceHelper.authResponseFromStore != null) {
            sharedPreferenceHelper.setHasEverLogged()
        }

        if (sharedPreferenceHelper.isEverLogged) return

        val now = DateTimeHelper.nowUtc()
        val oldTimestamp = sharedPreferenceHelper.registrationRemindTimestamp

        val scheduleMillis = if (now < oldTimestamp) {
            oldTimestamp
        } else {
            if (oldTimestamp == 0L) { // means that notification wasn't shown before
                now + AppConstants.MILLIS_IN_1HOUR
            } else {
                now + 2 * AppConstants.MILLIS_IN_1HOUR
            }
        }
        scheduleNotificationAt(scheduleMillis)
        sharedPreferenceHelper.saveRegistrationRemindTimestamp(scheduleMillis)
    }
}