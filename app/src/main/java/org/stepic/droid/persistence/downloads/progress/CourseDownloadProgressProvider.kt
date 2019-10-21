package org.stepic.droid.persistence.downloads.progress

import io.reactivex.Observable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.downloads.progress.mapper.DownloadProgressStatusMapper
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Course
import javax.inject.Inject
import javax.inject.Named

@PersistenceScope
class CourseDownloadProgressProvider
@Inject
constructor(
    updatesObservable: Observable<Structure>,
    intervalUpdatesObservable: Observable<Unit>,

    systemDownloadsDao: SystemDownloadsDao,
    persistentItemDao: PersistentItemDao,
    persistentStateManager: PersistentStateManager,
    @Named("content_screen")
    downloadProgressStatusMapper: DownloadProgressStatusMapper
): DownloadProgressProviderBase<Course>(updatesObservable, intervalUpdatesObservable, systemDownloadsDao, persistentItemDao, persistentStateManager, downloadProgressStatusMapper), DownloadProgressProvider<Course> {
    override fun Course.getId(): Long = id

    override val Structure.keyFieldValue: Long
        get() = course

    override val persistentItemKeyFieldColumn: String =
        DBStructurePersistentItem.Columns.COURSE

    override val persistentStateType: PersistentState.Type =
        PersistentState.Type.COURSE
}