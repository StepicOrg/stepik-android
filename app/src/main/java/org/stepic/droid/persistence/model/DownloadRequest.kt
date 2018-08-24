package org.stepic.droid.persistence.model

data class DownloadRequest(
        val task: DownloadTask,
        val title: String,
        val configuration: DownloadConfiguration
)