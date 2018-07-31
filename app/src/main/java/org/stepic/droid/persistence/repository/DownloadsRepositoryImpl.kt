package org.stepic.droid.persistence.repository

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.*
import org.stepic.droid.persistence.repository.progress.countItemProgress
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.util.merge
import javax.inject.Inject

@PersistenceScope
class DownloadsRepositoryImpl
@Inject
constructor(
        private val updatesObservable: Observable<PersistentItem>,
        private val intervalUpdatesObservable: Observable<kotlin.Unit>,

        private val systemDownloadsDao: SystemDownloadsDao,
        private val persistentItemDao: PersistentItemDao
): DownloadsRepository {
    override fun getDownloads(): Observable<DownloadItem> =
            intervalUpdatesObservable
                    .flatMap { getAllPersistentItems() }    // get all downloads with interval
                    .merge(updatesObservable.map(::listOf)) // get some instant updates on item changes
                    .flatMap(::fetchProgressesForCorrectItems)

    private fun getAllPersistentItems() =
            persistentItemDao.getItems(emptyMap())

    private fun fetchProgressesForCorrectItems(persistentItems: List<PersistentItem>): Observable<DownloadItem> {
        val correctItems = persistentItems.filter { it.status.isCorrect }
        val ids = correctItems.map { it.downloadId }.toLongArray()

        return systemDownloadsDao.get(*ids).flatMap { downloadItems ->
            countProgressForPersistentItems(correctItems, downloadItems)
        }
    }

    private fun countProgressForPersistentItems(persistentItems: List<PersistentItem>, systemDownloadItems: List<SystemDownloadRecord>) =
            persistentItems.mapNotNull { persistentItem ->
                val downloadItem = systemDownloadItems.find { persistentItem.downloadId == it.id } ?: return@mapNotNull null
                val progressItem = DownloadProgress(downloadItem.id, countItemProgress(emptyList(), listOf(downloadItem)))
                DownloadItem(persistentItem, progressItem)
            }.toObservable()
}