package org.stepik.android.view.notification.service

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import org.stepik.android.view.notification.StepikNotifManager
import javax.inject.Inject

class BootCompleteService : JobIntentService() {

    @Inject
    internal lateinit var notificationManager: StepikNotifManager

    companion object {
        private const val JOB_ID = 1000

        fun enqueueWork(context: Context, intent: Intent) {
            JobIntentService.enqueueWork(context, BootCompleteService::class.java, JOB_ID, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        notificationManager.rescheduleActiveNotifications()
    }
}