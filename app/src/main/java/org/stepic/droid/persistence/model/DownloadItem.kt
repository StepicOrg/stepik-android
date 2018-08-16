package org.stepic.droid.persistence.model

import org.stepik.android.model.Video

data class DownloadItem(
        val step: Long,
        val title: String,
        val video: Video,
        val bytesDownloaded: Long,
        val bytesTotal: Long,
        val status: DownloadProgress.Status
): Comparable<DownloadItem> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownloadItem

        if (step != other.step) return false

        return true
    }

    fun isCompletelyEquals(other: DownloadItem): Boolean {
        if (this === other) return true

        if (step != other.step) return false
        if (title != other.title) return false
        if (video != other.video) return false
        if (bytesDownloaded != other.bytesDownloaded) return false
        if (bytesTotal != other.bytesTotal) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int = step.hashCode()

    override fun compareTo(other: DownloadItem) =
            step.compareTo(other.step)
}