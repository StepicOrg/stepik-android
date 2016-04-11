package org.stepic.droid.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import org.stepic.droid.base.MainApplication
import org.stepic.droid.util.AppConstants
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class NotificationBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var notificationManager: INotificationManager

    @Inject
    lateinit var threadPool: ThreadPoolExecutor

    init {
        MainApplication.component().inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == AppConstants.NOTIFICATION_CANCELED) {
            val courseId = intent?.extras?.getLong(AppConstants.COURSE_ID_KEY)
            courseId?.let {
                val task = object : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg params: Void): Void? {
                        notificationManager.discardAllNotifications(it)
                        return null
                    }
                }
                task.executeOnExecutor(threadPool)
            }
        }
    }
}