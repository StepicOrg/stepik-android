package org.stepik.android.view.notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.view.notification.service.BootCompleteService

class StepikBootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            BootCompleteService.enqueueWork(context, intent)
        }
    }
}