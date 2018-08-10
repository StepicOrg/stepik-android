package org.stepic.droid.persistence.downloads

import android.app.DownloadManager
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.SystemDownloadRecord
import org.stepic.droid.persistence.storage.PersistentItemObserver
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.util.zip
import javax.inject.Inject

@PersistenceScope
class DownloadsSyncronizer
@Inject
constructor(
        private val persistentItemDao: PersistentItemDao,
        private val systemDownloadsDao: SystemDownloadsDao,

        private val intervalUpdatesObservable: Observable<Unit>,
        private val updatesObservable: Observable<PersistentItem>,

        private val persistentItemObserver: PersistentItemObserver,

        @BackgroundScheduler
        private val scheduler: Scheduler
) {
    init {
        initWatcher()
    }

    private fun initWatcher() {
        updatesObservable.map { kotlin.Unit }.startWith(kotlin.Unit)
                .switchMap { _ ->
                    intervalUpdatesObservable.startWith(kotlin.Unit).concatMap {
                        persistentItemDao.getItems(mapOf(DBStructurePersistentItem.Columns.STATUS to PersistentItem.Status.IN_PROGRESS.name))
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

    private fun syncPersistentItems(items: List<PersistentItem>, records: List<SystemDownloadRecord>) {
        records.filter { it.status == DownloadManager.STATUS_FAILED }.forEach { record ->
            items.find { it.downloadId == record.id }
                    ?.copy(status = PersistentItem.Status.DOWNLOAD_ERROR)
                    ?.let(persistentItemObserver::update)
        }
    }
}