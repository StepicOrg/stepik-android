package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import javax.inject.Inject

@PersistenceScope
class RemovalDownloadsInteractor
@Inject
constructor(
        private val persistentItemDao: PersistentItemDao,
        private val downloadTaskHelper: DownloadTaskHelper
) {
    fun removeAllDownloads(): Completable = downloadTaskHelper.removeTasks(
            persistentItemDao.getItems(emptyMap())
                    .flatMap(List<PersistentItem>::toObservable)
                    .map { it.task.structure }
    )

    fun removeDownloads(downloads: List<DownloadItem>): Completable =
            downloadTaskHelper.removeTasks(downloads.toObservable().map(DownloadItem::structure))
}