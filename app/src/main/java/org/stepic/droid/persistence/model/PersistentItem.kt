package org.stepic.droid.persistence.model

class PersistentItem(
        val originalPath: String,
        val localPath: String,

        val downloadId: Long,
        val status: Status,

        val course: Long,
        val section: Long,
        val unit: Long,
        val lesson: Long,
        val step: Long
) {
    enum class Status {
        PENDING, FILE_TRANSFER, COMPLETED, CANCELLED,
        DOWNLOAD_ERROR, TRANSFER_ERROR
    }
}