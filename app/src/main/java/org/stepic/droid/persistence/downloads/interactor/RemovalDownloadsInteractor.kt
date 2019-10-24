package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.downloads.helpers.RemoveDownloadTaskHelper
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import javax.inject.Inject

@PersistenceScope
class RemovalDownloadsInteractor
@Inject
constructor(
    private val persistentItemDao: PersistentItemDao,
    private val removeDownloadTaskHelper: RemoveDownloadTaskHelper
) {
    fun removeAllDownloads(): Completable =
        removeDownloadTaskHelper.removeTasks(
            persistentItemDao
                .getItems(emptyMap())
                .flatMapObservable(List<PersistentItem>::toObservable)
                .map { it.task.structure }
        )
}