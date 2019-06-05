package org.stepik.android.view.notification.service

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import org.stepic.droid.base.App
import org.stepik.android.view.notification.NotificationPublisher
import javax.inject.Inject

class NotificationAlarmService : JobIntentService() {
    @Inject
    internal lateinit var notificationPublisher: NotificationPublisher

    companion object {
        private const val JOB_ID = 1100

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, NotificationAlarmService::class.java, JOB_ID, intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        App.component().inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        val action = intent.action ?: return
        notificationPublisher.onNeedShowNotificationWithId(action)
    }
}