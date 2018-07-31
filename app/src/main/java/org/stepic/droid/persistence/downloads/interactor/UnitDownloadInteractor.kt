package org.stepic.droid.persistence.downloads.interactor

import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.adapters.DownloadTaskAdapter
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Unit
import javax.inject.Inject

@PersistenceScope
class UnitDownloadInteractor
@Inject
constructor(
        downloadTaskAdapter: DownloadTaskAdapter<Unit>,
        downloadTaskManager: DownloadTaskManager,
        persistentItemDao: PersistentItemDao
): DownloadInteractorBase<Unit>(downloadTaskAdapter, downloadTaskManager, persistentItemDao) {
    override val Unit.keyFieldValue: Long
        get() = id
    override val keyFieldColumn: String = DBStructurePersistentItem.Columns.SECTION
}