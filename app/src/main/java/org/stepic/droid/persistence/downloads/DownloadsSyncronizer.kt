package org.stepic.droid.persistence.downloads

import android.app.DownloadManager
import android.content.Context
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.persistence.di.FSLock
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.model.SystemDownloadRecord
import org.stepic.droid.persistence.service.DownloadCompleteService
import org.stepic.droid.persistence.storage.PersistentItemObserver
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.util.then
import org.stepic.droid.util.zip
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

@PersistenceScope
class DownloadsSyncronizer
@Inject
constructor(
        private val context: Context,
        private val persistentStateManager: PersistentStateManager,
        private val persistentItemDao: PersistentItemDao,
        private val systemDownloadsDao: SystemDownloadsDao,

        private val intervalUpdatesObservable: Observable<Unit>,
        private val updatesObservable: Observable<Structure>,

        private val persistentItemObserver: PersistentItemObserver,

        private val downloadErrorPoster: DownloadErrorPoster,
        private val externalStorageManager: ExternalStorageManager,

        @BackgroundScheduler
        private val scheduler: Scheduler,

        @FSLock
        private val fsLock: ReentrantLock
) {
    init {
        initWatcher()
    }

    private fun initWatcher() {
        (fixInconsistency() then updatesObservable).map { kotlin.Unit }.startWith(kotlin.Unit)
                .switchMap { _ ->
                    intervalUpdatesObservable.startWith(kotlin.Unit).concatMap {
                        persistentItemDao.getItemsByStatus(PersistentItem.Status.IN_PROGRESS)
                    }.takeWhile(List<PersistentItem>::isNotEmpty).concatMap {
                        Observable.just(it) zip systemDownloadsDao.get(*it.map(PersistentItem::downloadId).toLongArray())
                    }.map { (items, records) ->
                        syncPersistentItems(items, records)
                    }
                }
                .observeOn(scheduler)
                .subscribeOn(scheduler)
                .subscribeBy(onError = { initWatcher() }) // on error restart
    }

    private fun fixInconsistency() = Completable.fromAction {
        fixInTransferItems()
        fixInProgressItems()
    }

    private fun fixInTransferItems() = fsLock.withLock {
        val itemsInTransfer = persistentItemDao.getItemsByStatus(PersistentItem.Status.FILE_TRANSFER).blockingFirst()
        itemsInTransfer.forEach { item ->
            val path = externalStorageManager.resolvePathForPersistentItem(item.copy(status = PersistentItem.Status.COMPLETED))

            persistentItemObserver.update(item.copy(status = if (path != null) {
                PersistentItem.Status.COMPLETED
            } else {
                PersistentItem.Status.CANCELLED
            }))
        }
    }

    private fun fixInProgressItems() {
        persistentStateManager.resetInProgressItems()
    }

    private fun syncPersistentItems(items: List<PersistentItem>, records: List<SystemDownloadRecord>) {
        items.forEach { item ->
            val record = records.find { item.downloadId == it.id } ?: return@forEach
            when (record.status) {
                DownloadManager.STATUS_FAILED -> {
                    persistentItemObserver.update(item.copy(status = PersistentItem.Status.DOWNLOAD_ERROR))
                    downloadErrorPoster.onError(record)
                }

                DownloadManager.STATUS_SUCCESSFUL -> // redeliver completed download
                    DownloadCompleteService.enqueueWork(context, item.downloadId)
            }
        }
    }
}