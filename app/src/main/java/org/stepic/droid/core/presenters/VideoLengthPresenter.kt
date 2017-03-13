package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.VideoLengthResolver
import org.stepic.droid.core.presenters.contracts.VideoLengthView
import org.stepic.droid.model.Step
import org.stepic.droid.model.Video
import org.stepic.droid.util.resolvers.VideoResolver
import java.util.concurrent.ThreadPoolExecutor

class VideoLengthPresenter(private val threadPoolExecutor: ThreadPoolExecutor,
                           private val mainHandler: MainHandler,
                           private val videoResolver: VideoResolver,
                           private val videoLengthResolver: VideoLengthResolver) : PresenterBase<VideoLengthView>() {

    private companion object {
        val colon = ":"
    }

    var cachedFormat: String? = null

    fun fetchLength(video: Video, step: Step) {
        cachedFormat?.let {
            view?.onVideoLengthDetermined(it)
            return
        }
        threadPoolExecutor.execute {
            val path = videoResolver.resolveVideoUrl(video, step)
            val millis = videoLengthResolver.determineLengthInMillis(path) ?: return@execute
            // if not determine millis -> do not form printable string

            val printable = getPrintableFormat(millis)
            mainHandler.post {
                cachedFormat = printable
                view?.onVideoLengthDetermined(printable)
            }
        }
    }

    private fun getPrintableFormat(millis: Long): String {
        val durationInSeconds = millis / 1000
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds - hours * 3600) / 60
        val seconds = durationInSeconds - (hours * 3600 + minutes * 60)

        val stringBuilder = StringBuilder()
        stringBuilder.apply {
            if (hours > 0) {
                append(hours)
                append(colon)
                append(String.format("%02d", minutes)) // 2 digits always
                append(colon)

            } else {
                // no hours -> 1 or 2 digits minutes
                append(minutes)
                append(colon)
            }

            append(String.format("%02d", seconds)) // 2 digits always for seconds
        }
        return stringBuilder.toString()
    }
}
