package org.stepic.droid.persistence.model

class SystemDownloadRecord(
        val id: Long,
        val bytesDownloaded: Int,
        val bytesTotal: Int,
        val status: Int,
        val localUri: String
)