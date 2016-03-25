package org.stepic.droid.model

data class DownloadReportItem(
        val mBytesDownloaded: Int,
        val mBytesTotal: Int,
        val mColumnStatus: Int,
        val mDownloadId: Int,
        val mColumnReason: Int
)