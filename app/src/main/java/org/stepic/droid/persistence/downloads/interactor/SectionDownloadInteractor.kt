package org.stepic.droid.persistence.downloads.interactor

import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.adapters.DownloadTaskAdapter
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Section
import javax.inject.Inject

@PersistenceScope
class SectionDownloadInteractor
@Inject
constructor(
        downloadTaskAdapter: DownloadTaskAdapter<Section>,
        downloadTaskManager: DownloadTaskManager,
        persistentItemDao: PersistentItemDao
): DownloadInteractorBase<Section>(downloadTaskAdapter, downloadTaskManager, persistentItemDao) {
    override val Section.keyFieldValue: Long
        get() = id
    override val keyFieldColumn: String = DBStructurePersistentItem.Columns.SECTION
}