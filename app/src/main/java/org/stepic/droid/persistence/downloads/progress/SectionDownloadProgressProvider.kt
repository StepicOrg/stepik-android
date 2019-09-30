package org.stepic.droid.persistence.downloads.progress

import io.reactivex.Observable
import org.stepic.droid.persistence.di.PersistenceProgressStatusMapper
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.downloads.progress.mapper.DownloadProgressStatusMapper
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Section
import javax.inject.Inject

@PersistenceScope
class SectionDownloadProgressProvider
@Inject
constructor(
        updatesObservable: Observable<Structure>,
        intervalUpdatesObservable: Observable<Unit>,

        systemDownloadsDao: SystemDownloadsDao,
        persistentItemDao: PersistentItemDao,
        persistentStateManager: PersistentStateManager,
        @PersistenceProgressStatusMapper
        downloadStatusProgressStatusMapper: DownloadProgressStatusMapper
): DownloadProgressProviderBase<Section>(updatesObservable, intervalUpdatesObservable, systemDownloadsDao, persistentItemDao, persistentStateManager, downloadStatusProgressStatusMapper), DownloadProgressProvider<Section> {
    override fun Section.getId(): Long = id

    override val Structure.keyFieldValue: Long
        get() = section

    override val persistentItemKeyFieldColumn =
            DBStructurePersistentItem.Columns.SECTION

    override val persistentStateType: PersistentState.Type =
            PersistentState.Type.SECTION
}