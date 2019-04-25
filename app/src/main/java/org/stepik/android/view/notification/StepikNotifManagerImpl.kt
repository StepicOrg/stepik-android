package org.stepik.android.view.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class StepikNotifManagerImpl
@Inject constructor(
    private val context: Context,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val threadPoolExecutor: ThreadPoolExecutor
) : StepikNotifManager {

    private val alarmManager: AlarmManager by lazy { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private val notifcationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun scheduleNotification(id: String, millis: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rescheduleActiveNotifications() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}