package org.stepic.droid.persistence.downloads.progress.mapper

import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.SystemDownloadRecord

/**
 * Download progress priority:
 * in progress - if any is in progress
 * pending - if all is pending
 * not_cached - if any not completed
 * cached - if all is completed
 */
interface DownloadProgressStatusMapper {
    fun countItemProgress(
        persistentItems: List<PersistentItem>,
        downloadRecords: List<SystemDownloadRecord>,
        itemState: PersistentState.State
    ): DownloadProgress.Status
}