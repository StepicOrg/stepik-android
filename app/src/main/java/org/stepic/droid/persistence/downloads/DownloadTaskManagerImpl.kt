package org.stepic.droid.persistence.downloads

import android.app.DownloadManager
import android.net.Uri
import io.reactivex.Completable
import org.stepic.droid.persistence.di.FSLock
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.PersistentItemObserver
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
        private val persistentItemObserver: PersistentItemObserver,
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

        persistentItemObserver.update(PersistentItem(
                task = task,
                downloadId = downloadId,
                status = PersistentItem.Status.IN_PROGRESS
        ))
    }

    override fun removeTasks(items: List<PersistentItem>): Completable = Completable.fromAction {
        fsLock.withLock {
            items.forEach {
                persistentItemObserver.update(it.copy(status = PersistentItem.Status.FILE_TRANSFER))
            }

            items.forEach { item ->
                downloadManager.remove(item.downloadId)
                externalStorageManager.resolvePathForPersistentItem(item)?.let {
                    val file = File(it)
                    if (file.exists() && !file.delete()) {
                        throw IOException("Can't remove file: $it")
                    }
                }

                persistentItemObserver.update(item.copy(status = PersistentItem.Status.CANCELLED))
            }
        }
    }
}