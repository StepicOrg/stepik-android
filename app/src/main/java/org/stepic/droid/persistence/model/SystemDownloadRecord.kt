package org.stepic.droid.persistence.model

data class SystemDownloadRecord(
        val id: Long,
        val title: String,
        val bytesDownloaded: Int,
        val bytesTotal: Int,
        val status: Int,
        val reason: Int,
        val localUri: String
)