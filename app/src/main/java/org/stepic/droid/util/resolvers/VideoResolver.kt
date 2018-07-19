package org.stepic.droid.util.resolvers

import android.support.annotation.AnyThread
import org.stepik.android.model.Video

interface VideoResolver {

    @AnyThread
    fun resolveVideoUrl(video: Video?, isForPlaying : Boolean= true): String?
}
