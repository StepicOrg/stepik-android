package org.stepik.android.view.notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.view.notification.service.NotificationAlarmService

class StepikAlarmReceiver : BroadcastReceiver(){
    companion object {
        const val REQUEST_CODE = 151

        fun createIntent(context: Context, action: String): Intent =
                Intent(context, StepikAlarmReceiver::class.java)
                        .setAction(action)
    }

    override fun onReceive(context: Context, intent: Intent) {
        NotificationAlarmService.enqueueWork(context, intent)
    }
}