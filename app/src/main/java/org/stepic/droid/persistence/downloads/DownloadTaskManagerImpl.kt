package org.stepic.droid.persistence.downloads

import android.app.DownloadManager
import android.net.Uri
import io.reactivex.Completable
import io.reactivex.Observer
import org.stepic.droid.persistence.di.FSLock
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

@PersistenceScope
class DownloadTaskManagerImpl
@Inject
constructor(
        private val downloadManager: DownloadManager,
        private val updatesObserver: Observer<PersistentItem>,
        private val persistentItemDao: PersistentItemDao,
        private val externalStorageManager: ExternalStorageManager,

        @FSLock
        private val fsLock: ReentrantLock
): DownloadTaskManager {
    override fun addTask(task: DownloadTask, configuration: DownloadConfiguration): Completable = Completable.fromCallable {
        val request = DownloadManager.Request(Uri.parse(task.originalPath))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedNetworkTypes(configuration.allowedNetworkTypes.map(DownloadConfiguration.NetworkType::systemNetworkType).reduce(Int::or))
                .setTitle("Downloading") // todo add title

        val downloadId = downloadManager.enqueue(request)

        val persistentItem = PersistentItem(
                task = task,
                downloadId = downloadId,
                status = PersistentItem.Status.IN_PROGRESS
        )
        persistentItemDao.insertOrReplace(persistentItem)
        updatesObserver.onNext(persistentItem)
    }

    override fun removeTask(downloadId: Long): Completable = Completable.fromAction {
        fsLock.withLock {
            downloadManager.remove(downloadId)

            val item = persistentItemDao.get(mapOf(DBStructurePersistentItem.Columns.DOWNLOAD_ID to downloadId.toString())) ?: return@fromAction

            if (item.status == PersistentItem.Status.COMPLETED) {
                externalStorageManager.resolvePathForPersistentItem(item)?.let {
                    val file = File(it)
                    if (file.exists() && !file.delete()) {
                        throw IOException("Can't remove file: $it")
                    }
                }
            }

            persistentItemDao.remove(DBStructurePersistentItem.Columns.DOWNLOAD_ID, downloadId.toString())
            updatesObserver.onNext(item.copy(status = PersistentItem.Status.CANCELLED))
        }
    }
}