package org.stepic.droid.persistence.storage

import io.reactivex.Observer
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import javax.inject.Inject

@PersistenceScope
class PersistentItemObserverImpl
@Inject
constructor(
    private val persistentItemDao: PersistentItemDao,
    private val updatesObserver: Observer<Structure>
): PersistentItemObserver {
    override fun update(item: PersistentItem) {
        persistentItemDao.insertOrReplace(item)
        updatesObserver.onNext(item.task.structure)
    }

    override fun remove(item: PersistentItem) {
        persistentItemDao.remove(mapOf(
            DBStructurePersistentItem.Columns.STEP to item.task.structure.step.toString(),
            DBStructurePersistentItem.Columns.ORIGINAL_PATH to item.task.originalPath
        ))
        updatesObserver.onNext(item.task.structure)
    }
}