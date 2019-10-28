package org.stepic.droid.persistence.downloads.progress

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.toFlowable
import org.stepic.droid.di.qualifiers.PersistenceProgressStatusMapper
import org.stepic.droid.persistence.downloads.progress.mapper.DownloadProgressStatusMapper
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.model.SystemDownloadRecord
import org.stepic.droid.persistence.model.isCorrect
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao

abstract class DownloadProgressProviderBase<T>(
    private val updatesObservable: Observable<Structure>,
    private val intervalUpdatesObservable: Observable<kotlin.Unit>,

    private val systemDownloadsDao: SystemDownloadsDao,
    private val persistentItemDao: PersistentItemDao,
    private val persistentStateManager: PersistentStateManager,

    @PersistenceProgressStatusMapper
    private val downloadProgressStatusMapper: DownloadProgressStatusMapper
): DownloadProgressProvider<T> {
    private companion object {
        private fun List<PersistentItem>.getDownloadIdsOfCorrectItems(): LongArray =
            this.filter { it.status.isCorrect }.map { it.downloadId }.toLongArray()
    }

    override fun getProgress(vararg items: T): Flowable<DownloadProgress> =
        getProgress(*items.map { it.getId() }.toLongArray())

    override fun getProgress(vararg ids: Long): Flowable<DownloadProgress> =
        ids.toFlowable().flatMap(::getItemProgress)

    private fun getItemProgress(itemId: Long): Flowable<DownloadProgress> =
        getItemUpdateObservable(itemId)
            .switchMap {
                intervalUpdatesObservable.startWith(kotlin.Unit)
                    .concatMapSingle { getItemProgressFromDB(itemId) }
                    .takeUntil { it.status is DownloadProgress.Status.Cached || it.status == DownloadProgress.Status.NotCached }
            }
            .toFlowable(BackpressureStrategy.LATEST)

    private fun getItemProgressFromDB(itemId: Long): Single<DownloadProgress> =
        Single
            .fromCallable {
                persistentStateManager.getState(itemId, persistentStateType)
            }
            .flatMap { state ->
                when (state) {
                    PersistentState.State.IN_PROGRESS ->
                        Single.just(DownloadProgress(itemId, DownloadProgress.Status.Pending))
                    else ->
                        getPersistentItems(itemId)
                            .flatMap(::fetchSystemDownloads)
                            .map { (persistentItems, downloadItems) ->   // count progresses
                                DownloadProgress(itemId, downloadProgressStatusMapper.countItemProgress(persistentItems, downloadItems, state))
                            }
                }
            }

    private fun getPersistentItems(itemId: Long): Single<List<PersistentItem>> =
        persistentItemDao.getItems(mapOf(persistentItemKeyFieldColumn to itemId.toString()))

    private fun fetchSystemDownloads(items: List<PersistentItem>): Single<Pair<List<PersistentItem>, List<SystemDownloadRecord>>> =
        zip(Single.just(items), systemDownloadsDao.get(*items.getDownloadIdsOfCorrectItems()))

    private fun getItemUpdateObservable(itemId: Long): Observable<kotlin.Unit> =
        updatesObservable.filter { it.keyFieldValue == itemId }.map { kotlin.Unit }.startWith(kotlin.Unit)

    protected abstract fun T.getId(): Long
    protected abstract val Structure.keyFieldValue: Long
    protected abstract val persistentItemKeyFieldColumn: String
    protected abstract val persistentStateType: PersistentState.Type
}