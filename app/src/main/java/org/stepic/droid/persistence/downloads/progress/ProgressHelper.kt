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
    if (persistentItems.isEmpty()) return DownloadProgress.Status.NotCached

    var downloaded = 0
    var total = 0

    downloadRecords.forEach { item ->
        if (item.bytesTotal > 0) {
            downloaded += item.bytesDownloaded
            total += item.bytesTotal
        }
    }

    var hasCompletedItems = false
    var hasItemsInTransfer = false
    var hasUndownloadedItems = false
    persistentItems.forEach { item ->
        when(item.status) {
            PersistentItem.Status.IN_PROGRESS -> return if (total <= 0) {
                DownloadProgress.Status.Pending
            } else {
                DownloadProgress.Status.InProgress(downloaded.toFloat() / total)
            }

            PersistentItem.Status.COMPLETED ->
                hasCompletedItems = true

            PersistentItem.Status.FILE_TRANSFER ->
                hasItemsInTransfer = true

            else ->
                hasUndownloadedItems = true
        }
    }

    return when {
        hasItemsInTransfer ->
            DownloadProgress.Status.Pending

        hasUndownloadedItems ->
            DownloadProgress.Status.NotCached

        else ->
            DownloadProgress.Status.Cached
    }
}