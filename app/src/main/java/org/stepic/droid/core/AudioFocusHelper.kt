package org.stepic.droid.core

import android.content.Context
import android.media.AudioManager
import org.stepic.droid.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class AudioFocusHelper
@Inject constructor(context: Context) {

    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun requestAudioFocus(listener: AudioManager.OnAudioFocusChangeListener) = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
            audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)


    fun releaseAudioFocus(listener: AudioManager.OnAudioFocusChangeListener) = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
            audioManager.abandonAudioFocus(listener);

}