package org.stepic.droid.di.video

import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class VideoModule {
    private val ON_VIDEO_OPEN_REWIND_MILLIS = 1500L

    @Provides
    @VideoScope
    @Named(Companion.rewindOnOpenName)
    fun provideRewindOnOpen(): Long {
        return ON_VIDEO_OPEN_REWIND_MILLIS
    }


    companion object {
        const val rewindOnOpenName = "rewindOnOpen"
    }
}
