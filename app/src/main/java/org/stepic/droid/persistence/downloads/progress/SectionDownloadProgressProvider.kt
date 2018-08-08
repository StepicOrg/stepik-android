package org.stepic.droid.persistence.downloads.progress

import io.reactivex.Observable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
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
        updatesObservable: Observable<PersistentItem>,
        intervalUpdatesObservable: Observable<Unit>,

        systemDownloadsDao: SystemDownloadsDao,
        persistentItemDao: PersistentItemDao,
        persistentStateManager: PersistentStateManager
): DownloadProgressProviderBase<Section>(updatesObservable, intervalUpdatesObservable, systemDownloadsDao, persistentItemDao, persistentStateManager), DownloadProgressProvider<Section> {
    override fun Section.getId(): Long = id

    override val PersistentItem.keyFieldValue: Long
        get() = task.structure.section

    override val persistentItemKeyFieldColumn =
            DBStructurePersistentItem.Columns.SECTION

    override val persistentStateType: PersistentState.Type =
            PersistentState.Type.SECTION
}