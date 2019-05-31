package org.stepic.droid.persistence.downloads.helpers

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
import org.stepik.android.domain.step.repository.StepRepository
import javax.inject.Inject

@PersistenceScope
class AddDownloadTaskHelperImpl
@Inject
constructor(
    private val downloadTaskManager: DownloadTaskManager,
    private val persistentStateManager: PersistentStateManager,

    private val stepRepository: StepRepository,
    private val stepContentResolver: StepContentResolver,
    private val downloadTitleResolver: DownloadTitleResolver,

    private val persistentItemDao: PersistentItemDao
) : AddDownloadTaskHelper {
    override fun addTasks(structureObservable: Observable<Structure>, configuration: DownloadConfiguration): Completable =
        structureObservable
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
        stepRepository
            .getStep(structure.step)
            .map { step ->
                val paths = stepContentResolver.getDownloadableContentFromStep(step, configuration)
                val pathsFiltered = cleanUpPreviousTasks(structure.step, paths)
                pathsFiltered.map { DownloadTask(it, structure) }
            }
            .flatMapObservable(List<DownloadTask>::toObservable)
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

        downloadTaskManager.removeTasks(itemsToRemove, shouldRemoveFromDb = true).blockingAwait()
        return paths.filterNot(alreadyDownloadedPaths::contains)
    }
}