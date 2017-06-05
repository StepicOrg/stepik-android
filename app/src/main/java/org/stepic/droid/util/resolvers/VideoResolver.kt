package org.stepic.droid.util.resolvers

import android.support.annotation.WorkerThread
import org.stepic.droid.model.Video

interface VideoResolver {

    @WorkerThread
    fun resolveVideoUrl(video : Video?): String?
}
