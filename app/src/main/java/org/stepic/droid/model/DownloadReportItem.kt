package org.stepic.droid.model

data class DownloadReportItem(
        val bytesDownloaded: Int,
        val bytesTotal: Int,
        val mColumnStatus: Int,
        val mDownloadId: Int,
        val mColumnReason: Int
)