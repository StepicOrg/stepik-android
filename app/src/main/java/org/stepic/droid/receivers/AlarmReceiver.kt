package org.stepic.droid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepic.droid.services.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 177

        fun createIntent(context: Context, action: String): Intent =
            Intent(context, AlarmReceiver::class.java)
                .setAction(action)
    }

    override fun onReceive(context: Context, intent: Intent) {
        AlarmService.enqueueWork(context, intent)
    }
}