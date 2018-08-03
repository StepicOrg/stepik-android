package org.stepic.droid.persistence.downloads

import android.app.DownloadManager
import android.net.Uri
import io.reactivex.Completable
import io.reactivex.Observer
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import javax.inject.Inject

@PersistenceScope
class DownloadTaskManagerImpl
@Inject
constructor(
        private val downloadManager: DownloadManager,
        private val updatesObserver: Observer<PersistentItem>,
        private val persistentItemDao: PersistentItemDao
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

    override fun removeTask(downloadId: Long): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}