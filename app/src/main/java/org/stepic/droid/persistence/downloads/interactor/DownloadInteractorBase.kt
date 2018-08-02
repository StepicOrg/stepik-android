package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.adapters.DownloadTaskAdapter
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.dao.PersistentItemDao

abstract class DownloadInteractorBase<T>(
        private val downloadTaskAdapter: DownloadTaskAdapter<T>,
        private val downloadTaskManager: DownloadTaskManager,
        private val persistentItemDao: PersistentItemDao
): DownloadInteractor<T> {
    override fun addTask(vararg ids: Long, configuration: DownloadConfiguration): Completable =
            downloadTaskAdapter.convertToTask(*ids, configuration = configuration).flatMapCompletable {
                downloadTaskManager.addTask(it, configuration)
            }

    override fun addTask(vararg items: T, configuration: DownloadConfiguration): Completable =
            downloadTaskAdapter.convertToTask(*items, configuration = configuration).flatMapCompletable {
                downloadTaskManager.addTask(it, configuration)
            }

    override fun removeTask(item: T): Completable =
            removeTask(item.keyFieldValue)

    override fun removeTask(id: Long): Completable =
            persistentItemDao
                    .getItems(mapOf(keyFieldColumn to id.toString()))
                    .flatMap(List<PersistentItem>::toObservable)
                    .flatMapCompletable {
                        downloadTaskManager.removeTask(it.downloadId)
                    }

    protected abstract val T.keyFieldValue: Long
    protected abstract val keyFieldColumn: String
}