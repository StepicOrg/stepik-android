package org.stepic.droid.persistence.downloads.interactor

import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitDownloadInteractor
@Inject
constructor(
        structureResolver: StructureResolver<Unit>,
        downloadTaskManager: DownloadTaskManager,
        persistentItemDao: PersistentItemDao,

        persistentStateManager: PersistentStateManager,

        stepContentResolver: StepContentResolver,
        stepRepository: Repository<Step>
): DownloadInteractorBase<Unit>(structureResolver, downloadTaskManager, persistentItemDao, persistentStateManager, stepContentResolver, stepRepository) {
    override val Unit.keyFieldValue: Long
        get() = id
    override val keyFieldColumn: String = DBStructurePersistentItem.Columns.UNIT
}