package org.stepic.droid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepic.droid.services.NewUserAlarmService

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 177

        fun createIntent(context: Context, action: String): Intent =
            Intent(context, AlarmReceiver::class.java)
                .setAction(action)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            NewUserAlarmService.enqueueWork(context, intent)
        }
    }
}