package org.stepic.droid.core

import android.content.Context
import android.media.AudioManager
import com.squareup.otto.Bus
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.events.audio.AudioFocusGainEvent
import org.stepic.droid.events.audio.AudioFocusLossEvent

class AudioFocusHelper(val context: Context, val bus: Bus, val mainHandler: MainHandler) : AudioManager.OnAudioFocusChangeListener {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun requestAudioFocus() = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)


    fun releaseAudioFocus() = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
            audioManager.abandonAudioFocus(this);


    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> mainHandler.post { bus.post(AudioFocusGainEvent()) }
            AudioManager.AUDIOFOCUS_LOSS -> mainHandler.post { bus.post(AudioFocusLossEvent()) }
        }
    }
}