package org.stepic.droid.persistence.downloads.interactor

import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Section
import javax.inject.Inject

class SectionDownloadInteractor
@Inject
constructor(
        structureResolver: StructureResolver<Section>,
        persistentItemDao: PersistentItemDao,

        downloadTasksHelper: DownloadTaskHelper
): DownloadInteractorBase<Section>(structureResolver, persistentItemDao, downloadTasksHelper) {
    override val Section.keyFieldValue: Long
        get() = id
    override val keyFieldColumn: String = DBStructurePersistentItem.Columns.SECTION
}