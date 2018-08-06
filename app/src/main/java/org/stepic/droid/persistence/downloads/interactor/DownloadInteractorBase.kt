package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.adapters.DownloadTaskAdapter
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.isCorrect
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem

abstract class DownloadInteractorBase<T>(
        private val downloadTaskAdapter: DownloadTaskAdapter<T>,
        private val downloadTaskManager: DownloadTaskManager,
        private val persistentItemDao: PersistentItemDao
): DownloadInteractor<T> {
    override fun addTask(vararg ids: Long, configuration: DownloadConfiguration): Completable =
            addTasks(downloadTaskAdapter.convertToTask(*ids, configuration = configuration), configuration)

    override fun addTask(vararg items: T, configuration: DownloadConfiguration): Completable =
            addTasks(downloadTaskAdapter.convertToTask(*items, configuration = configuration), configuration)

    private fun addTasks(tasksObservable: Observable<DownloadTask>, configuration: DownloadConfiguration) = tasksObservable
            .toList()
            .map(::cleanUpPreviousTasks)
            .flatMapObservable(List<DownloadTask>::toObservable)
            .flatMapCompletable {
                downloadTaskManager.addTask(it, configuration)
            }

    private fun cleanUpPreviousTasks(tasks: List<DownloadTask>): List<DownloadTask> {
        val stepToPaths = tasks.groupBy(DownloadTask::step, DownloadTask::originalPath)

        val alreadyDownloadedPaths = mutableSetOf<String>()
        stepToPaths.forEach { step, paths ->
            val oldTasks = persistentItemDao.getAll(mapOf(DBStructurePersistentItem.Columns.STEP to step.toString()))

            oldTasks.forEach { item ->
                if (item.task.originalPath in paths && item.status.isCorrect) {
                    alreadyDownloadedPaths.add(item.task.originalPath)
                } else {
                    downloadTaskManager.removeTask(item.downloadId).blockingAwait()
                }
            }
        }

        return tasks.filter { !alreadyDownloadedPaths.contains(it.originalPath) }
    }

    override fun removeTask(item: T): Completable =
            removeTask(item.keyFieldValue)

    override fun removeTask(id: Long): Completable =
            persistentItemDao
                    .getItems(mapOf(keyFieldColumn to id.toString()))
                    .flatMap(List<PersistentItem>::toObservable)
                    .flatMapCompletable {
                        downloadTaskManager.removeTask(it.downloadId)
                    }

    protected abstract val T.keyFieldValue: Long
    protected abstract val keyFieldColumn: String
}