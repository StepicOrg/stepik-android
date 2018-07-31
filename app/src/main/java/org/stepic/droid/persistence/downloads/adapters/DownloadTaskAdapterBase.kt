package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Completable
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao

abstract class DownloadTaskAdapterBase(
    private val persistentItemDao: PersistentItemDao,

    private val downloadTaskManager: DownloadTaskManager
): DownloadTaskAdapter {
    override fun removeTask(id: Long): Completable =
            persistentItemDao
                    .getItem(mapOf(persistentItemKeyFieldColumn to id.toString()))
                    .flatMapCompletable(downloadTaskManager::removeTask)

    protected abstract val persistentItemKeyFieldColumn: String
}