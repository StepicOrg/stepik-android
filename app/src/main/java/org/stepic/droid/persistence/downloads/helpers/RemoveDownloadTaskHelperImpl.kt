package org.stepic.droid.persistence.downloads.helpers

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import javax.inject.Inject

@PersistenceScope
class RemoveDownloadTaskHelperImpl
@Inject
constructor(
        private val downloadTaskManager: DownloadTaskManager,
        private val persistentStateManager: PersistentStateManager,
        private val persistentItemDao: PersistentItemDao
) : RemoveDownloadTaskHelper {
    override fun removeTasks(structureObservable: Observable<Structure>): Completable =
            structureObservable
                    .toList()
                    .doOnSuccess { // in order to get rid of blinking on delete operation
                        it.forEach { structure ->
                            persistentStateManager.invalidateStructure(structure, PersistentState.State.IN_PROGRESS)
                        }
                    }
                    .flatMapObservable(List<Structure>::toObservable)
                    .flatMapCompletable { structure ->
                        persistentItemDao.getItemsByStep(structure.step) // as step is smallest atom
                                .concatMapCompletable { downloadTaskManager.removeTasks(it) }
                                .doFinally {
                                    persistentStateManager.invalidateStructure(structure, PersistentState.State.NOT_CACHED)
                                }
                    }
}