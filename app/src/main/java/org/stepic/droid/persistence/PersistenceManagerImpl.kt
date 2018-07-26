package org.stepic.droid.persistence

import android.app.DownloadManager
import android.content.Context
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.ItemUpdateEvent
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.ProgressItem
import org.stepic.droid.persistence.storage.DownloadItemDao
import org.stepic.droid.persistence.storage.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.util.merge
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.model.Step
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppSingleton
class PersistenceManagerImpl
@Inject
constructor(
        context: Context,
        private val persistentItemDao: PersistentItemDao,

        private val sectionsRepository: Repository<Section>,
        private val unitsRepository: Repository<Unit>,
        private val lessonsRepository: Repository<Lesson>,
        private val stepsRepository: Repository<Step>,

        private val downloadManager: DownloadManager,

        private val downloadItemDao: DownloadItemDao
): PersistenceManager {
    companion object {
        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    }

    private val observableUpdateTick = Observable.interval(PROGRESS_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS).map { kotlin.Unit }.share()
    private val itemsUpdatesSubject = PublishSubject.create<ItemUpdateEvent>()

    override fun getSectionsProgress(vararg sectionsIds: Long): Observable<ProgressItem> =
            getItemsProgress(sectionsIds, ItemUpdateEvent.Type.SECTION, persistentItemDao::getItemsBySectionId)

    override fun getUnitsProgress(vararg unitsIds: Long): Observable<ProgressItem> =
            getItemsProgress(unitsIds, ItemUpdateEvent.Type.UNIT, persistentItemDao::getItemsByUnitId)


    private inline fun getItemsProgress(
            itemsIds: LongArray,
            itemsType: ItemUpdateEvent.Type,
            crossinline persistentSelector: (Long) -> Observable<List<PersistentItem>>
    ) = itemsIds.toObservable().flatMap { getItemProgress(it, itemsType, persistentSelector(it)) }

    private fun getItemProgress(itemId: Long, itemType: ItemUpdateEvent.Type, persistentObservable: Observable<List<PersistentItem>>) =
            (itemsUpdatesSubject.filter { it.type == itemType && it.id == itemId }.map { kotlin.Unit } merge observableUpdateTick) // listen for updates
                    .flatMap { persistentObservable } // fetch from DB
                    .flatMap { items -> // fetch progresses
                        val ids = items.filter {
                            it.status == PersistentItem.Status.PENDING
                                    || it.status == PersistentItem.Status.FILE_TRANSFER
                                    || it.status == PersistentItem.Status.COMPLETED
                        }.map { it.downloadId }.toLongArray()

                        downloadItemDao.get(*ids)
                    }.map { // count progresses
                        countItemProgress(itemId, it)
                    }.distinctUntilChanged() // exclude repetitive events

    private fun countItemProgress(itemId: Long, downloadItems: List<DownloadItem>): ProgressItem {
        var downloaded = 0
        var total = 0

        downloadItems.forEach { item ->
            if (item.bytesTotal > 0) {
                downloaded += item.bytesDownloaded
                total += item.bytesTotal
            }
        }

        return when {
            total == 0 ->
                if (downloadItems.isEmpty()) {
                    ProgressItem.NotCached(itemId)
                } else {
                    ProgressItem.Pending(itemId)
                }

            downloaded == total ->
                ProgressItem.Cached(itemId)

            else ->
                ProgressItem.InProgress(itemId, downloaded.toFloat() / total)
        }
    }


    override fun addCacheSectionTask(sectionId: Long): Completable = TODO()
    override fun addCacheUnitTask(unitId: Long): Completable = TODO()

    private fun getDownloadableContentFromStep(step: Step): List<String> =
            step.block?.video?.urls?.firstOrNull()?.url?.let(::listOf) ?: emptyList()

    override fun onDownloadCompleted(downloadId: Long, localPath: String): Completable = Completable.fromCallable {
        val item = persistentItemDao.get(DBStructurePersistentItem.Columns.DOWNLOAD_ID, downloadId.toString())
        if (item != null) {
            persistentItemDao.insertOrUpdate(item.copy(localPath = localPath, status = PersistentItem.Status.COMPLETED))
        }
    }

    override fun resolvePath(originalPath: String): Maybe<String> =
            persistentItemDao.getItemByPath(originalPath).flatMap {
                if (File(it.localPath).exists()) {
                    Maybe.just(it.localPath)
                } else {
                    persistentItemDao.remove(DBStructurePersistentItem.Columns.LOCAL_PATH, it.localPath)
                    Maybe.empty()
                }
            }
}