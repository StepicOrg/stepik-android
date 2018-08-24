package org.stepic.droid.persistence.downloads.progress

import io.reactivex.Observable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Unit
import javax.inject.Inject

@PersistenceScope
class UnitDownloadProgressProvider
@Inject
constructor(
        updatesObservable: Observable<Structure>,
        intervalUpdatesObservable: Observable<kotlin.Unit>,

        systemDownloadsDao: SystemDownloadsDao,
        persistentItemDao: PersistentItemDao,
        persistentStateManager: PersistentStateManager
): DownloadProgressProviderBase<Unit>(updatesObservable, intervalUpdatesObservable, systemDownloadsDao, persistentItemDao, persistentStateManager), DownloadProgressProvider<Unit> {
    override fun Unit.getId(): Long = id

    override val Structure.keyFieldValue: Long
        get() = unit

    override val persistentItemKeyFieldColumn =
            DBStructurePersistentItem.Columns.UNIT

    override val persistentStateType: PersistentState.Type =
            PersistentState.Type.UNIT
}