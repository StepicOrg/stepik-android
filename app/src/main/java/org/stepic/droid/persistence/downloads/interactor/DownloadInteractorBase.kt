package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepic.droid.persistence.model.*
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Step

abstract class DownloadInteractorBase<T>(
        private val structureResolver: StructureResolver<T>,
        private val downloadTaskManager: DownloadTaskManager,
        private val persistentItemDao: PersistentItemDao,

        private val persistentStateManager: PersistentStateManager,

        private val stepContentResolver: StepContentResolver,
        private val stepRepository: Repository<Step>
): DownloadInteractor<T> {
    override fun addTask(vararg ids: Long, configuration: DownloadConfiguration): Completable =
            addTasks(structureResolver.resolveStructure(*ids), configuration)

    override fun addTask(vararg items: T, configuration: DownloadConfiguration): Completable =
            addTasks(structureResolver.resolveStructure(*items), configuration)

    private fun addTasks(structureObservable: Observable<Structure>, configuration: DownloadConfiguration) = structureObservable
            .doOnNext { persistentStateManager.invalidateStructure(it, PersistentState.State.IN_PROGRESS) }
            .flatMapCompletable { structure ->
                resolveStructureContent(structure, configuration)
                        .flatMap(List<DownloadTask>::toObservable)
                        .flatMapCompletable {
                            downloadTaskManager.addTask(it, configuration)
                        }.doOnComplete {
                            persistentStateManager.invalidateStructure(structure, PersistentState.State.CACHED)
                        }.doOnError {
                            persistentStateManager.invalidateStructure(structure, PersistentState.State.NOT_CACHED)
                        }.doOnDispose {
                            persistentStateManager.invalidateStructure(structure, PersistentState.State.NOT_CACHED)
                        }
            }

    private fun resolveStructureContent(structure: Structure, configuration: DownloadConfiguration): Observable<List<DownloadTask>> =
            Observable.fromCallable {
                val step = stepRepository.getObject(structure.step)!! // without maps to reduce overhead
                val paths = stepContentResolver.getDownloadableContentFromStep(step, configuration)
                val pathsFiltered = cleanUpPreviousTasks(structure.step, paths)
                return@fromCallable pathsFiltered.map { DownloadTask(it, structure) }
            }

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

    override fun removeTask(item: T): Completable =
            removeTask(item.keyFieldValue)

    override fun removeTask(id: Long): Completable =
            structureResolver.resolveStructure(id)
                    .toList()
                    .doOnSuccess { // in order to get rid of blinking on delete operation
                        it.forEach { structure ->
                            persistentStateManager.invalidateStructure(structure, PersistentState.State.IN_PROGRESS)
                        }
                    }
                    .flatMapObservable(List<Structure>::toObservable)
                    .flatMapCompletable { structure ->
                        persistentItemDao
                                .getItems(mapOf(keyFieldColumn to id.toString()))
                                .concatMapCompletable(downloadTaskManager::removeTasks)
                                .doFinally {
                                    persistentStateManager.invalidateStructure(structure, PersistentState.State.NOT_CACHED)
                                }
                    }

    protected abstract val T.keyFieldValue: Long
    protected abstract val keyFieldColumn: String
}