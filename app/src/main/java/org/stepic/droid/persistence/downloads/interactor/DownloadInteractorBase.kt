package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepic.droid.persistence.model.*
import org.stepic.droid.persistence.storage.dao.PersistentItemDao

abstract class DownloadInteractorBase<T>(
        private val structureResolver: StructureResolver<T>,
        private val persistentItemDao: PersistentItemDao,

        private val downloadTasksHelper: DownloadTaskHelper
): DownloadInteractor<T> {
    override fun addTask(vararg ids: Long, configuration: DownloadConfiguration): Completable =
            downloadTasksHelper.addTasks(structureResolver.resolveStructure(*ids), configuration)

    override fun addTask(vararg items: T, configuration: DownloadConfiguration): Completable =
            downloadTasksHelper.addTasks(structureResolver.resolveStructure(*items), configuration)

    override fun removeTask(item: T): Completable =
            removeTask(item.keyFieldValue)

    override fun removeTask(id: Long): Completable =
            downloadTasksHelper.removeTasks(
                    structureResolver.resolveStructure(id),
                    persistentItemDao.getItems(mapOf(keyFieldColumn to id.toString()))
            )

    protected abstract val T.keyFieldValue: Long
    protected abstract val keyFieldColumn: String
}