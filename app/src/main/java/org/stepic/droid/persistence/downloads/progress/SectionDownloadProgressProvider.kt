package org.stepic.droid.persistence.downloads.progress

import io.reactivex.Observable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import javax.inject.Inject

@PersistenceScope
class SectionDownloadProgressProvider
@Inject
constructor(
        updatesObservable: Observable<PersistentItem>,
        intervalUpdatesObservable: Observable<Unit>,

        systemDownloadsDao: SystemDownloadsDao,
        persistentItemDao: PersistentItemDao
): DownloadProgressProviderBase(updatesObservable, intervalUpdatesObservable, systemDownloadsDao, persistentItemDao), DownloadProgressProvider {
    override val PersistentItem.keyFieldValue: Long
        get() = section

    override val persistentItemKeyFieldColumn =
            DBStructurePersistentItem.Columns.SECTION
}