//package org.stepic.droid.persistence
//
//import android.app.DownloadManager
//import io.reactivex.Completable
//import io.reactivex.Maybe
//import io.reactivex.Observable
//import io.reactivex.rxkotlin.toObservable
//import org.stepic.droid.di.AppSingleton
//import org.stepic.droid.persistence.model.*
//import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
//import org.stepic.droid.persistence.storage.dao.PersistentItemDao
//import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
//import org.stepic.droid.storage.repositories.Repository
//import org.stepic.droid.util.merge
//import org.stepic.droid.util.zip
//import org.stepik.android.model.Lesson
//import org.stepik.android.model.Section
//import org.stepik.android.model.Unit
//import org.stepik.android.model.Step
//import java.io.File
//import java.util.concurrent.TimeUnit
//import javax.inject.Inject
//
//@AppSingleton
//class PersistenceManagerImpl
//@Inject
//constructor(
//        private val persistentItemDao: PersistentItemDao,
//        private val systemDownloadsDao: SystemDownloadsDao,
//
//        private val sectionsRepository: Repository<Section>,
//        private val unitsRepository: Repository<Unit>,
//        private val lessonsRepository: Repository<Lesson>,
//        private val stepsRepository: Repository<Step>,
//
//        private val downloadManager: DownloadManager,
//        private val updatesObservable: Observable<PersistentItem>
//): PersistenceManager {
//    companion object {
//        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L
//    }
//
//    private val observableUpdateTick = Observable.interval(PROGRESS_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS).map { kotlin.Unit }.share()
//
//    private fun getItemUpdateObservable(itemId: Long) =
//            (updatesObservable.filter { it.type == itemType && it.id == itemId }.map { kotlin.Unit } merge observableUpdateTick)
//
//    override fun getSectionsProgress(vararg sectionsIds: Long): Observable<DownloadProgress> =
//            getItemsProgress(sectionsIds)
//
//    override fun getUnitsProgress(vararg unitsIds: Long): Observable<DownloadProgress> =
//            getItemsProgress(unitsIds)
//
//
//    private val PersistentItemType.column
//            get() = when(this) {
//                PersistentItemType.UNIT    -> DBStructurePersistentItem.Columns.UNIT
//                PersistentItemType.SECTION -> DBStructurePersistentItem.Columns.SECTION
//            }
//
//
//    private fun getItemsProgress(
//            itemsIds: LongArray,
//            itemsType: PersistentItemType
//    ) = itemsIds.toObservable().flatMap { getItemProgress(it, itemsType, persistentItemDao.getItems(mapOf(itemsType.column to it.toString()))) }
//
//    private fun getItemProgress(itemId: Long, itemType: PersistentItemType, persistentObservable: Observable<List<PersistentItem>>) =
//            getItemUpdateObservable(itemId, itemType) // listen for updates
//                    .flatMap { persistentObservable } // fetch from DB
//                    .flatMap { items ->               // fetch progresses from system Download Manager
//                        Observable.just(items) zip systemDownloadsDao.get(*items.map { it.downloadId }.toLongArray())
//                    }.map { (persistentItems, downloadItems) -> // count progresses
//                        countItemProgress(itemId, persistentItems, downloadItems)
//                    }.distinctUntilChanged() // exclude repetitive events
//
//    private fun countItemProgress(itemId: Long, persistentItems: List<PersistentItem>, systemDownloadItems: List<SystemDownload>): DownloadProgress {
//        var downloaded = 0
//        var total = 0
//
//        systemDownloadItems.forEach { item ->
//            if (item.bytesTotal > 0) {
//                downloaded += item.bytesDownloaded
//                total += item.bytesTotal
//            }
//        }
//
//        val status = when {
//            total == 0 ->
//                if (systemDownloadItems.isEmpty()) {
//                    DownloadProgress.Status.NotCached
//                } else {
//                    DownloadProgress.Status.Pending
//                }
//
//            downloaded == total ->
//                DownloadProgress.Status.Cached
//
//            else ->
//                DownloadProgress.Status.InProgress(downloaded.toFloat() / total)
//        }
//        return DownloadProgress(id = itemId, status = status)
//    }
//
//
//    override fun onDownloadCompleted(downloadId: Long, localPath: String): Completable = Completable.fromCallable {
//        val item = persistentItemDao.get(DBStructurePersistentItem.Columns.DOWNLOAD_ID, downloadId.toString())
//        if (item != null) {
//            persistentItemDao.insertOrUpdate(item.copy(localPath = localPath, status = PersistentItem.Status.COMPLETED))
//        }
//    }
//
//    override fun resolvePath(originalPath: String): Maybe<String> =
//            persistentItemDao.getItem(mapOf(DBStructurePersistentItem.Columns.ORIGINAL_PATH to originalPath)).filter {
//                it.status == PersistentItem.Status.COMPLETED
//            }.flatMap {
//                if (File(it.localPath).exists()) {
//                    Maybe.just(it.localPath)
//                } else {
//                    persistentItemDao.remove(DBStructurePersistentItem.Columns.LOCAL_PATH, it.localPath)
//                    Maybe.empty()
//                }
//            }
//}