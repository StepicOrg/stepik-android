package org.stepik.android.domain.download.mapper

import org.stepic.droid.persistence.downloads.progress.mapper.DownloadProgressStatusMapper
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.SystemDownloadRecord
import java.io.File
import javax.inject.Inject
import kotlin.math.max

class DownloadProgressStatusMapperImpl
@Inject
constructor(
    private val externalStorageManager: ExternalStorageManager
) : DownloadProgressStatusMapper {
    override fun countItemProgress(
        persistentItems: List<PersistentItem>,
        downloadRecords: List<SystemDownloadRecord>,
        itemState: PersistentState.State
    ): DownloadProgress.Status {
        if (persistentItems.isEmpty()) {
            return when (itemState) {
                PersistentState.State.NOT_CACHED  -> DownloadProgress.Status.NotCached
                PersistentState.State.IN_PROGRESS -> DownloadProgress.Status.Pending
                PersistentState.State.CACHED      -> DownloadProgress.Status.Cached(bytesTotal = 0)
            }
        }
        var bytesDownloaded = 0L
        var bytesTotal = 0L

        val linksMap = mutableMapOf<String, String>()

        var hasItemsInProgress = false
        var hasItemsInCriticalSection = false

        persistentItems.forEach { item ->
            when (item.status) {
                PersistentItem.Status.COMPLETED -> {
                    val filePath = externalStorageManager.resolvePathForPersistentItem(item)
                    if (filePath == null) {
                        return@forEach
                    } else {
                        val fileSize = File(filePath).length()
                        bytesDownloaded += fileSize
                        bytesTotal += fileSize

                        linksMap[item.task.originalPath] = filePath
                    }
                }

                PersistentItem.Status.IN_PROGRESS,
                PersistentItem.Status.FILE_TRANSFER -> {
                    val record = downloadRecords.find { it.id == item.downloadId }
                    if (record == null) {
                        return@forEach
                    } else {
                        bytesDownloaded += record.bytesDownloaded
                        bytesTotal += max(record.bytesDownloaded, record.bytesTotal) // total could be 0
                        hasItemsInProgress = true
                        hasItemsInCriticalSection =
                            hasItemsInCriticalSection || item.status == PersistentItem.Status.FILE_TRANSFER
                    }
                }

                else -> {
                    return@forEach
                }
            }
        }

        return when {
            hasItemsInCriticalSection ->
                DownloadProgress.Status.Pending

            hasItemsInProgress ->
                DownloadProgress.Status.Cached(bytesDownloaded)

            bytesTotal == 0L ->
                DownloadProgress.Status.NotCached

            else ->
                DownloadProgress.Status.Cached(bytesTotal)
        }
    }
}