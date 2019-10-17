package org.stepik.android.view.notification.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import org.stepic.droid.base.App
import org.stepik.android.view.notification.NotificationDelegate
import javax.inject.Inject

class BootCompleteService : JobIntentService() {

    @Inject
    internal lateinit var notificationDelegates: Set<@JvmSuppressWildcards NotificationDelegate>

    companion object {
        private const val JOB_ID = 1000

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, BootCompleteService::class.java, JOB_ID, intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        App.component().inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        notificationDelegates.forEach { it.rescheduleNotification() }
    }
}