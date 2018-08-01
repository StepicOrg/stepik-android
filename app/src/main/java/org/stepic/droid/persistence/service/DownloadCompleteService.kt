package org.stepic.droid.persistence.service

import android.app.DownloadManager
import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.reactivex.Observer
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.ExternalStorageManager
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.persistence.model.SystemDownloadRecord
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import java.io.File
import java.io.IOException
import javax.inject.Inject

class DownloadCompleteService: IntentService("download_updates_service") {
    @Inject
    lateinit var systemDownloadsDao: SystemDownloadsDao

    @Inject
    lateinit var persistentItemDao: PersistentItemDao

    @Inject
    lateinit var externalStorageManager: ExternalStorageManager

    @Inject
    lateinit var updatesObserver: Observer<PersistentItem>

    @Inject
    lateinit var analytic: Analytic

    override fun onCreate() {
        super.onCreate()
        App.componentManager()
                .persistenceComponent
                .inject(this)
    }

    override fun onHandleIntent(intent: Intent) {
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                .takeIf { it != -1L } ?: return

        val download = systemDownloadsDao.get(downloadId).blockingFirst().firstOrNull()
        val persistentItem = persistentItemDao.get(mapOf(DBStructurePersistentItem.Columns.DOWNLOAD_ID to downloadId.toString()))
                ?: return

        when {
            download == null ->
                updatePersistentItem(persistentItem.copy(status = PersistentItem.Status.CANCELLED))

            download.status == DownloadManager.STATUS_SUCCESSFUL ->
                moveDownloadedFile(download, persistentItem)

            else ->
                analytic.reportEvent(Analytic.DownloaderV2.RECEIVE_BAD_DOWNLOAD_STATUS,
                        Bundle(1).apply { putInt(Analytic.DownloaderV2.Params.DOWNLOAD_STATUS, download.status) })
        }
    }

    private fun updatePersistentItem(persistentItem: PersistentItem) {
        persistentItemDao.insertOrUpdate(persistentItem)
        updatesObserver.onNext(persistentItem)
    }

    private fun moveDownloadedFile(downloadRecord: SystemDownloadRecord, persistentItem: PersistentItem) {
        try {
            val targetFileName = "${downloadRecord.id}_${persistentItem.task.step}_${System.nanoTime()}" // in order to generate unique file name if downloadId will be the same

            val targetDir = externalStorageManager.getSelectedStorageLocation()
            val targetFile = File(targetDir.path, targetFileName)

            val newPersistentItem = persistentItem.copy(
                    localFileName = targetFileName,
                    localFileDir = targetDir.path.canonicalPath,
                    isInAppInternalDir = targetDir.type == StorageLocation.Type.APP_INTERNAL,
                    status = PersistentItem.Status.FILE_TRANSFER
            )

            updatePersistentItem(newPersistentItem)

            if (targetFile.exists()) {
                if (!targetFile.delete()) throw IOException("Can't delete previous file")
                if (!targetFile.createNewFile()) throw IOException("Can't create new file")
            }

            applicationContext.contentResolver.openInputStream(Uri.parse(downloadRecord.localUri)).use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            updatePersistentItem(newPersistentItem.copy(
                    status = PersistentItem.Status.COMPLETED
            ))
        } catch (_: Exception) {
            updatePersistentItem(persistentItem.copy(
                    status = PersistentItem.Status.TRANSFER_ERROR
            ))
        }
    }
}