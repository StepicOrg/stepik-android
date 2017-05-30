package org.stepic.droid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class HeadPhoneReceiver : BroadcastReceiver() {

    public var listener: HeadPhoneListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            AudioManager.ACTION_AUDIO_BECOMING_NOISY ->
                listener?.onUnplugHeadphones()
        }
    }

    interface HeadPhoneListener {
        fun onUnplugHeadphones()
    }
}