package org.stepic.droid.util.resolvers

import android.support.annotation.AnyThread
import org.stepic.droid.model.Video

interface VideoResolver {

    @AnyThread
    fun resolveVideoUrl(video: Video?, isForPlaying : Boolean= true): String?
}
