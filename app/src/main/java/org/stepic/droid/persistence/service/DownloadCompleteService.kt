package org.stepic.droid.persistence.service

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.JobIntentService
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.di.FSLock
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.persistence.model.SystemDownloadRecord
import org.stepic.droid.persistence.storage.PersistentItemObserver
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class DownloadCompleteService: JobIntentService() {
    companion object {
        private const val JOB_ID = 2000

        fun enqueueWork(context: Context, downloadId: Long) {
            enqueueWork(context, DownloadCompleteService::class.java, JOB_ID,
                    Intent().putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId))
        }
    }

    @Inject
    lateinit var systemDownloadsDao: SystemDownloadsDao

    @Inject
    lateinit var persistentItemDao: PersistentItemDao

    @Inject
    lateinit var externalStorageManager: ExternalStorageManager

    @Inject
    lateinit var persistentItemObserver: PersistentItemObserver

    @Inject
    lateinit var analytic: Analytic

    @Inject
    @field:FSLock
    lateinit var fsLock: ReentrantLock

    @Inject
    lateinit var downloadManager: DownloadManager

    override fun onCreate() {
        super.onCreate()
        App.component().inject(this)
    }

    override fun onHandleWork(intent: Intent) = fsLock.withLock {
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                .takeIf { it != -1L } ?: return

        val download = systemDownloadsDao.get(downloadId).blockingFirst().firstOrNull()
        val persistentItem = persistentItemDao.get(mapOf(
                DBStructurePersistentItem.Columns.DOWNLOAD_ID to downloadId.toString(),
                DBStructurePersistentItem.Columns.STATUS      to PersistentItem.Status.IN_PROGRESS.name // move file only one time
        )) ?: return

        when {
            download == null -> // download was cancelled with system UI
                persistentItemObserver.update(persistentItem.copy(status = PersistentItem.Status.CANCELLED))

            download.status == DownloadManager.STATUS_SUCCESSFUL && persistentItem.status == PersistentItem.Status.IN_PROGRESS -> // ok
                moveDownloadedFile(download, persistentItem)

            else -> {
                downloadManager.remove(downloadId)
                analytic.reportEventWithName(Analytic.DownloaderV2.RECEIVE_BAD_DOWNLOAD_STATUS,
                        "system status = ${download.status} / status = ${persistentItem.status}")
            }
        }
    }

    private fun moveDownloadedFile(downloadRecord: SystemDownloadRecord, persistentItem: PersistentItem) {
        try {
            val targetFileName = "${downloadRecord.id}_${persistentItem.task.structure.step}_${System.nanoTime()}"
            // in order to generate unique file name if downloadId will be the same

            val targetDir = externalStorageManager.getSelectedStorageLocation()
            val targetFile = File(targetDir.path, targetFileName)

            val newPersistentItem = persistentItem.copy(
                    localFileName = targetFileName,
                    localFileDir = targetDir.path.canonicalPath,
                    isInAppInternalDir = targetDir.type == StorageLocation.Type.APP_INTERNAL,
                    status = PersistentItem.Status.FILE_TRANSFER
            )

            persistentItemObserver.update(newPersistentItem)

            if (targetFile.exists()) {
                if (!targetFile.delete()) throw IOException("Can't delete previous file")
                if (!targetFile.createNewFile()) throw IOException("Can't create new file")
            }

            applicationContext.contentResolver.openInputStream(Uri.parse(downloadRecord.localUri)).use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            persistentItemObserver.update(newPersistentItem.copy(
                    status = PersistentItem.Status.COMPLETED
            ))

            downloadManager.remove(downloadRecord.id)
        } catch (_: Exception) {
            persistentItemObserver.update(persistentItem.copy(
                    status = PersistentItem.Status.TRANSFER_ERROR
            ))
        }
    }
}