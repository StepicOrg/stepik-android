package org.stepik.android.domain.step_content_video.mapper

import javax.inject.Inject

class VideoLengthMapper
@Inject
constructor() {
    companion object {
        private const val COLON = ":"
    }

    fun mapVideoLengthFromMsToString(videoLengthMs: Long): String {
        val durationInSeconds = videoLengthMs / 1000

        val hours = durationInSeconds / 3600
        val minutesAndSecondsInSeconds = durationInSeconds % 3600
        val minutes = minutesAndSecondsInSeconds / 60
        val seconds = minutesAndSecondsInSeconds % 60

        val stringBuilder = StringBuilder()
        stringBuilder.apply {
            if (hours > 0) {
                append(hours)
                append(COLON)
                append(String.format("%02d", minutes)) // 2 digits always
                append(COLON)
            } else {
                // no hours -> 1 or 2 digits minutes
                append(minutes)
                append(COLON)
            }

            append(String.format("%02d", seconds)) // 2 digits always for seconds
        }
        return stringBuilder.toString()
    }
}