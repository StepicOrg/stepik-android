package org.stepic.droid.persistence.downloads.progress

import org.stepic.droid.persistence.model.SystemDownloadRecord
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.DownloadProgress

/**
 * Download progress priority:
 * in progress - if any is in progress
 * pending - if all is pending
 * not_cached - if any not completed
 * cached - if all is completed
 */
internal fun countItemProgress(persistentItems: List<PersistentItem>, downloadRecords: List<SystemDownloadRecord>): DownloadProgress.Status {
    var downloaded = 0
    var total = 0

    downloadRecords.forEach { item ->
        if (item.bytesTotal > 0) {
            downloaded += item.bytesDownloaded
            total += item.bytesTotal
        }
    }

    val notAllDownloadsCompleted = persistentItems.any { it.status != PersistentItem.Status.COMPLETED }

    return when {
        total == 0 ->
            if (downloadRecords.isEmpty()) {
                DownloadProgress.Status.NotCached
            } else {
                DownloadProgress.Status.Pending
            }

        downloaded == total ->
            if (notAllDownloadsCompleted) {
                DownloadProgress.Status.NotCached
            } else {
                DownloadProgress.Status.Cached
            }

        else ->
            DownloadProgress.Status.InProgress(downloaded.toFloat() / total)
    }
}