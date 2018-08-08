package org.stepic.droid.persistence.downloads.interactor

import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.adapters.StructureResolver
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import javax.inject.Inject

class SectionDownloadInteractor
@Inject
constructor(
        structureResolver: StructureResolver<Section>,
        downloadTaskManager: DownloadTaskManager,
        persistentItemDao: PersistentItemDao,

        persistentStateManager: PersistentStateManager,

        stepContentResolver: StepContentResolver,
        stepRepository: Repository<Step>
): DownloadInteractorBase<Section>(structureResolver, downloadTaskManager, persistentItemDao, persistentStateManager, stepContentResolver, stepRepository) {
    override val Section.keyFieldValue: Long
        get() = id
    override val keyFieldColumn: String = DBStructurePersistentItem.Columns.SECTION
}