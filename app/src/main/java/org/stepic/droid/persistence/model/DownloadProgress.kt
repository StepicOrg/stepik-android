package org.stepic.droid.persistence.model

class DownloadProgress(
        val id: Long,
        val status: Status
) {
    sealed class Status {
        object NotCached: Status()
        object Cached: Status()
        object Pending: Status()
        class InProgress(val progress: Float): Status()
    }
}