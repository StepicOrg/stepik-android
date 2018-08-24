package org.stepic.droid.persistence.model

data class PersistentItem(
        val localFileName: String = "",

        val localFileDir: String = "",
        val isInAppInternalDir: Boolean = false,
        val downloadId: Long,

        val status: Status,
        val task: DownloadTask
) {
    enum class Status {
        IN_PROGRESS, FILE_TRANSFER, COMPLETED, CANCELLED,
        DOWNLOAD_ERROR, TRANSFER_ERROR
    }
}

val PersistentItem.Status.isTerminal: Boolean
        get() = this != PersistentItem.Status.IN_PROGRESS &&
                this != PersistentItem.Status.FILE_TRANSFER

val PersistentItem.Status.isCorrect: Boolean
        get() = this == PersistentItem.Status.IN_PROGRESS ||
                this == PersistentItem.Status.FILE_TRANSFER ||
                this == PersistentItem.Status.COMPLETED