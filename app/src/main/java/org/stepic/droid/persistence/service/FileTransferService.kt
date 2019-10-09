package org.stepic.droid.persistence.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.di.FSLock
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.persistence.storage.PersistentItemObserver
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class FileTransferService: JobIntentService() {
    sealed class Event {
        object TransferCompleted: Event()
    }

    companion object {
        private const val JOB_ID = 2001

        fun enqueueWork(context: Context) {
            enqueueWork(context, FileTransferService::class.java, JOB_ID, Intent())
        }
    }

    @Inject
    @field:FSLock
    lateinit var fsLock: ReentrantLock

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var persistentItemObserver: PersistentItemObserver

    @Inject
    lateinit var persistentItemDao: PersistentItemDao

    @Inject
    lateinit var persistentStateManager: PersistentStateManager

    @Inject
    lateinit var externalStorageManager: ExternalStorageManager

    @Inject
    lateinit var fileTransferEventSubject: PublishSubject<Event>

    override fun onCreate() {
        super.onCreate()
        App.component().inject(this)
    }

    override fun onHandleWork(intent: Intent) = fsLock.withLock {
        val storage = externalStorageManager.getSelectedStorageLocation()
        val items = persistentItemDao.getItemsByStatus(PersistentItem.Status.COMPLETED).blockingFirst()

        items.groupBy { it.task.structure.step }.entries.forEach { (_, downloads) ->
            val structure = downloads.first().task.structure
            persistentStateManager.invalidateStructure(structure, PersistentState.State.IN_PROGRESS)

            downloads.forEach {
                moveFile(it, storage)
            }

            persistentStateManager.invalidateStructure(structure, PersistentState.State.CACHED)
        }

        fileTransferEventSubject.onNext(Event.TransferCompleted)
    }

    private fun moveFile(persistentItem: PersistentItem, storage: StorageLocation) {
        try {
            persistentItemObserver.update(persistentItem.copy(status = PersistentItem.Status.FILE_TRANSFER))

            val path = externalStorageManager.resolvePathForPersistentItem(persistentItem)
                    ?: return persistentItemObserver.update(persistentItem.copy(status = PersistentItem.Status.CANCELLED))
            val file = File(path)

            val targetFile = File(storage.path, persistentItem.localFileName)

            if (file.canonicalPath != targetFile.canonicalPath) {
                file.copyTo(targetFile, overwrite = true)
                file.delete()
            }

            persistentItemObserver.update(persistentItem.copy(
                    status = PersistentItem.Status.COMPLETED,
                    localFileDir = storage.path.canonicalPath,
                    isInAppInternalDir = storage.type == StorageLocation.Type.APP_INTERNAL
            ))
        } catch (e: Exception) {
            persistentItemObserver.update(persistentItem.copy(status = PersistentItem.Status.COMPLETED)) // means we are not moved file but it OK
            analytic.reportError(Analytic.DownloaderV2.FILE_TRANSFER_ERROR, e)
        }
    }
}