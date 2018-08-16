package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.resolvers.DownloadTitleResolver
import org.stepic.droid.persistence.model.*
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Step
import javax.inject.Inject

@PersistenceScope
class DownloadTaskHelperImpl
@Inject
constructor(
        private val downloadTaskManager: DownloadTaskManager,
        private val persistentStateManager: PersistentStateManager,

        private val stepRepository: Repository<Step>,
        private val stepContentResolver: StepContentResolver,
        private val downloadTitleResolver: DownloadTitleResolver,

        private val persistentItemDao: PersistentItemDao
) : DownloadTaskHelper {
    override fun addTasks(structureObservable: Observable<Structure>, configuration: DownloadConfiguration): Completable = structureObservable
            .doOnNext { persistentStateManager.invalidateStructure(it, PersistentState.State.IN_PROGRESS) }
            .flatMapCompletable { structure ->
                resolveStructureContent(structure, configuration)
                        .flatMapCompletable(downloadTaskManager::addTask)
                        .doOnComplete {
                            persistentStateManager.invalidateStructure(structure, PersistentState.State.CACHED)
                        }.doOnError {
                            persistentStateManager.invalidateStructure(structure, PersistentState.State.NOT_CACHED)
                        }.doOnDispose {
                            persistentStateManager.invalidateStructure(structure, PersistentState.State.NOT_CACHED)
                        }
            }

    private fun resolveStructureContent(structure: Structure, configuration: DownloadConfiguration): Observable<DownloadRequest> =
            Observable.fromCallable {
                val step = stepRepository.getObject(structure.step)!! // without maps to reduce overhead
                val paths = stepContentResolver.getDownloadableContentFromStep(step, configuration)
                val pathsFiltered = cleanUpPreviousTasks(structure.step, paths)
                return@fromCallable pathsFiltered.map { DownloadTask(it, structure) }
            }
                    .flatMap(List<DownloadTask>::toObservable)
                    .flatMap({
                        downloadTitleResolver.resolveTitle(it.structure).toObservable()
                    }, {a, b -> DownloadRequest(a, b, configuration) })

    private fun cleanUpPreviousTasks(stepId: Long, paths: Iterable<String>): Iterable<String> {
        val alreadyDownloadedPaths = mutableSetOf<String>()
        val itemsToRemove = mutableListOf<PersistentItem>()

        val oldTasks = persistentItemDao.getAll(mapOf(DBStructurePersistentItem.Columns.STEP to stepId.toString()))

        oldTasks.forEach { item ->
            if (item.task.originalPath in paths && item.status.isCorrect) {
                alreadyDownloadedPaths.add(item.task.originalPath)
            } else {
                itemsToRemove.add(item)
            }
        }

        downloadTaskManager.removeTasks(itemsToRemove).blockingAwait()
        return paths.filter { !alreadyDownloadedPaths.contains(it) }
    }


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
                                .concatMapCompletable(downloadTaskManager::removeTasks)
                                .doFinally {
                                    persistentStateManager.invalidateStructure(structure, PersistentState.State.NOT_CACHED)
                                }
                    }
}