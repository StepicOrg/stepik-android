package org.stepik.android.data.download.repository

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.model.SystemDownloadRecord
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.util.plus
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.download.repository.DownloadRepository
import org.stepik.android.model.Course
import java.io.File
import javax.inject.Inject
import kotlin.math.max

class DownloadRepositoryImpl
@Inject
constructor(
    private val updatesObservable: Observable<Structure>,
    private val intervalUpdatesObservable: Observable<Unit>,

    private val systemDownloadsDao: SystemDownloadsDao,
    private val persistentItemDao: PersistentItemDao,
//    private val persistentStateManager: PersistentStateManager,
//    private val stepRepository: StepRepository,
//
//    private val downloadTitleResolver: DownloadTitleResolver,
    private val externalStorageManager: ExternalStorageManager,
    private val courseRepository: CourseRepository
) : DownloadRepository {
    override fun getDownloads(): Observable<DownloadItem> = (
        persistentItemDao.getAllCorrectItems()
            .flatMap { it.groupBy { item -> item.task.structure }.map { (a, b) -> a to b }.toObservable() } +
                updatesObservable
                    .flatMap { structure -> persistentItemDao.getItemsByCourse(structure.course).map { structure to it } }
        )
//        .map { (structure, items) ->
//            structure to if (persistentStateManager.getState(structure.course, PersistentState.Type.COURSE) == PersistentState.State.CACHED) {
//                items
//            } else {
//                emptyList()
//            }
//        }
        .flatMap { (structure, items) -> getDownloadItem(structure, items) }

    private fun getDownloadItem(structure: Structure, items: List<PersistentItem>): Observable<DownloadItem> = (
            resolveCourse(structure, items) +
                    intervalUpdatesObservable
                        .switchMap { persistentItemDao.getItemsByCourse(structure.course) }
                        .switchMap { resolveCourse(structure, it) }
            )
        .takeUntil { it.status !is DownloadProgress.Status.InProgress }

    private fun resolveCourse(structure: Structure, items: List<PersistentItem>): Observable<DownloadItem> =
        Observables.zip(
            courseRepository.getCourse(structure.course, canUseCache = true).toObservable(),
            getStorageRecords(items)
        ) { course, records ->
            resolveDownloadItem(course, items, records)
        }

//    private fun resolveStep(structure: Structure, items: List<PersistentItem>): Observable<DownloadItem> =
//        stepRepository
//            .getStep(structure.step, primarySourceType = DataSourceType.CACHE)
//            .flatMapObservable { step ->
//                Observables.zip(
//                    downloadTitleResolver.resolveTitle(step.lesson, step.id).toObservable(),
//                    getStorageRecords(items)
//                )
//            }
//            .map { (title, records) ->
//                resolveDownloadItem(items, records)
//            }
//
    private fun getStorageRecords(items: List<PersistentItem>) = Observable
        .fromCallable { items.filter { it.status == PersistentItem.Status.IN_PROGRESS || it.status == PersistentItem.Status.FILE_TRANSFER } }
        .flatMap { systemDownloadsDao.get(*it.map(PersistentItem::downloadId).toLongArray()) }

    private fun resolveDownloadItem(course: Course, items: List<PersistentItem>, records: List<SystemDownloadRecord>): DownloadItem {
        var bytesDownloaded = 0L
        var bytesTotal = 0L

        val linksMap = mutableMapOf<String, String>()

        var hasItemsInProgress = false
        var hasUndownloadedItems = items.isEmpty()

        items.forEach { item ->
            when(item.status) {
                PersistentItem.Status.COMPLETED -> {
                    val filePath = externalStorageManager.resolvePathForPersistentItem(item)
                    if (filePath == null) {
                        hasUndownloadedItems = true
                        return@forEach
                    } else {
                        val fileSize = File(filePath).length()
                        bytesDownloaded += fileSize
                        bytesTotal += fileSize

                        linksMap[item.task.originalPath] = filePath
                    }
                }

                PersistentItem.Status.IN_PROGRESS,
                PersistentItem.Status.FILE_TRANSFER -> {
                    val record = records.find { it.id == item.downloadId }
                    if (record == null) {
                        hasUndownloadedItems = true
                        return@forEach
                    } else {
                        bytesDownloaded += record.bytesDownloaded
                        bytesTotal += max(record.bytesDownloaded, record.bytesTotal) // total could be 0
                        hasItemsInProgress = true
                    }
                }

                else -> {
                    hasUndownloadedItems = true
                    return@forEach
                }
            }
        }

        val status = when {
            hasUndownloadedItems -> {
                DownloadProgress.Status.NotCached
            }

            hasItemsInProgress -> {
                val progress = if (bytesTotal <= 0) 0f else bytesDownloaded.toFloat() / bytesTotal
                DownloadProgress.Status.InProgress(progress)
            }

            else -> {
                DownloadProgress.Status.Cached(bytesTotal)
            }
        }

        return DownloadItem(course, status)
    }
}