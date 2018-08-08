package org.stepic.droid.persistence.model

data class SystemDownloadRecord(
        val id: Long,
        val bytesDownloaded: Int,
        val bytesTotal: Int,
        val status: Int,
        val localUri: String
)