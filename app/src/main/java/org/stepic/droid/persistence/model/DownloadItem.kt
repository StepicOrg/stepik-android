package org.stepic.droid.persistence.model

import org.stepik.android.model.Video

data class DownloadItem(
        val step: Long,
        val title: String,
        val video: Video,
        val bytesDownloaded: Long,
        val bytesTotal: Long,
        val progress: DownloadProgress
)