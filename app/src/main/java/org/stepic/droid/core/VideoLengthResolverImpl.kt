package org.stepic.droid.core

import android.media.MediaMetadataRetriever
import android.support.annotation.WorkerThread
import java.util.*
import javax.inject.Inject

class VideoLengthResolverImpl @Inject constructor() : VideoLengthResolver {

    @WorkerThread
    override fun determineLengthInMillis(path: String?): Long? {
        if (path.isNullOrBlank()) return null

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(path) //local
        } catch (exception: IllegalArgumentException) {
            try {
                retriever.setDataSource(path, HashMap<String, String>()) // internet
            } catch (internetException: Exception) {
                return null
            }
        }

        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillis = java.lang.Long.parseLong(time)
        return timeInMillis
    }

}
