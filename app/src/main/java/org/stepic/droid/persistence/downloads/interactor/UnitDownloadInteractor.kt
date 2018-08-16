package org.stepic.droid.persistence.downloads.interactor

import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitDownloadInteractor
@Inject
constructor(
        structureResolver: StructureResolver<Unit>,
        persistentItemDao: PersistentItemDao,

        downloadTasksHelper: DownloadTaskHelper
): DownloadInteractorBase<Unit>(structureResolver, persistentItemDao, downloadTasksHelper) {
    override val Unit.keyFieldValue: Long
        get() = id
    override val keyFieldColumn: String = DBStructurePersistentItem.Columns.UNIT
}