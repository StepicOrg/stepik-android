package org.stepik.android.view.notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepik.android.view.notification.service.BootCompleteService

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            BootCompleteService.enqueueWork(context, intent)
        }
    }
}