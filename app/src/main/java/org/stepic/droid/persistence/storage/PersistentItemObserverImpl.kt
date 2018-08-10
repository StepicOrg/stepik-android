package org.stepic.droid.persistence.storage

import io.reactivex.Observer
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
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
}