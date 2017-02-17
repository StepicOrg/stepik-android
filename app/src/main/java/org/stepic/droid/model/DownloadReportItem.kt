package org.stepic.droid.model

data class DownloadReportItem(
        val bytesDownloaded: Int,
        val bytesTotal: Int,
        val columnStatus: Int,
        val downloadId: Int,
        val columnReason: Int
)