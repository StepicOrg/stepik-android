package org.stepik.android.data.download.repository

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.downloads.progress.mapper.DownloadProgressStatusMapper
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.model.SystemDownloadRecord
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.util.mapToLongArray
import org.stepic.droid.util.plus
import org.stepik.android.data.download.source.DownloadCacheDataSource
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.download.repository.DownloadRepository
import org.stepik.android.model.Course
import org.stepik.android.view.injection.download.DownloadsProgressStatusMapper
import javax.inject.Inject

class DownloadRepositoryImpl
@Inject
constructor(
    private val updatesObservable: Observable<Structure>,
    private val intervalUpdatesObservable: Observable<Unit>,

    private val systemDownloadsDao: SystemDownloadsDao,
    private val persistentItemDao: PersistentItemDao,
    private val courseRepository: CourseRepository,
    @DownloadsProgressStatusMapper
    private val downloadProgressStatusMapper: DownloadProgressStatusMapper,
    private val downloadCacheDataSource: DownloadCacheDataSource
) : DownloadRepository {
    override fun getDownloads(): Observable<DownloadItem> =
        Observable
            .merge(
                downloadCacheDataSource
                    .getDownloadedCoursesIds()
                    .flatMapObservable { it.toObservable() },
                updatesObservable
                    .map { it.course }
            )
            .flatMap { courseId -> persistentItemDao.getItemsByCourse(courseId).map { courseId to it } }
            .flatMap { (courseId, items) -> getDownloadItem(courseId, items) }

    private fun getDownloadItem(courseId: Long, items: List<PersistentItem>): Observable<DownloadItem> =
        (resolveCourse(courseId, items).toObservable() +
                    intervalUpdatesObservable
                        .switchMap { persistentItemDao.getItemsByCourse(courseId) }
                        .switchMapSingle { resolveCourse(courseId, it) }
            )
        .takeUntil { it.status !is DownloadProgress.Status.InProgress }

    private fun resolveCourse(courseId: Long, items: List<PersistentItem>): Single<DownloadItem> =
        Singles.zip(
            courseRepository.getCourse(courseId, canUseCache = true).toSingle(),
            getStorageRecords(items)
        ) { course, records ->
            resolveDownloadItem(course, items, records)
        }

    private fun getStorageRecords(items: List<PersistentItem>): Single<List<SystemDownloadRecord>> =
        Single
            .fromCallable { items.filter { it.status == PersistentItem.Status.IN_PROGRESS || it.status == PersistentItem.Status.FILE_TRANSFER } }
            .flatMap { systemDownloadsDao.get(*it.mapToLongArray(PersistentItem::downloadId)) }

    private fun resolveDownloadItem(course: Course, items: List<PersistentItem>, records: List<SystemDownloadRecord>): DownloadItem =
        DownloadItem(course, downloadProgressStatusMapper.countItemProgress(items, records, PersistentState.State.CACHED))
}