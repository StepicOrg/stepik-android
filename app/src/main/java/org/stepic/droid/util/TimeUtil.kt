package org.stepic.droid.util

object TimeUtil {
    private const val colon = ":"

    fun getFormattedVideoTime(millis: Long): String {
        val durationInSeconds = millis / 1000

        val hours = durationInSeconds / 3600
        val minutesAndSecondsInSeconds = durationInSeconds % 3600
        val minutes = minutesAndSecondsInSeconds / 60
        val seconds = minutesAndSecondsInSeconds % 60

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
