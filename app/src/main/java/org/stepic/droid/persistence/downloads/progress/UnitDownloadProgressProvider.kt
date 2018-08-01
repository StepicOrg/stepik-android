package org.stepic.droid.persistence.downloads.progress

import io.reactivex.Observable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Unit
import javax.inject.Inject

@PersistenceScope
class UnitDownloadProgressProvider
@Inject
constructor(
        updatesObservable: Observable<PersistentItem>,
        intervalUpdatesObservable: Observable<kotlin.Unit>,

        systemDownloadsDao: SystemDownloadsDao,
        persistentItemDao: PersistentItemDao
): DownloadProgressProviderBase<Unit>(updatesObservable, intervalUpdatesObservable, systemDownloadsDao, persistentItemDao), DownloadProgressProvider<Unit> {
    override fun Unit.getId(): Long = id

    override val PersistentItem.keyFieldValue: Long
        get() = task.unit

    override val persistentItemKeyFieldColumn =
            DBStructurePersistentItem.Columns.UNIT
}