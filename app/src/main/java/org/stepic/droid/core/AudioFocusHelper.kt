package org.stepic.droid.core

import android.content.Context
import android.media.AudioManager
import com.squareup.otto.Bus
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.events.audio.AudioFocusGainEvent
import org.stepic.droid.events.audio.AudioFocusLossEvent

class AudioFocusHelper(context: Context, bus: Bus, mainHandler: IMainHandler) : AudioManager.OnAudioFocusChangeListener {
    val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val mContext = context
    val mBus = bus
    val mMainHandler = mainHandler

    fun requestAudioFocus() = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
            mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)


    fun releaseAudioFocus() = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
            mAudioManager.abandonAudioFocus(this);


    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> mMainHandler.post { mBus.post(AudioFocusGainEvent()) }
            AudioManager.AUDIOFOCUS_LOSS -> mMainHandler.post { mBus.post(AudioFocusLossEvent()) }
        }
    }
}