package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import org.stepic.droid.persistence.downloads.helpers.AddDownloadTaskHelper
import org.stepic.droid.persistence.downloads.helpers.RemoveDownloadTaskHelper
import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepic.droid.persistence.model.*

class DownloadInteractorBase<T>(
        private val structureResolver: StructureResolver<T>,
        private val addDownloadTasksHelper: AddDownloadTaskHelper,
        private val removeDownloadTaskHelper: RemoveDownloadTaskHelper
): DownloadInteractor<T> {
    override fun addTask(vararg ids: Long, configuration: DownloadConfiguration): Completable =
            addDownloadTasksHelper.addTasks(structureResolver.resolveStructure(*ids), configuration)

    override fun addTask(vararg items: T, configuration: DownloadConfiguration): Completable =
            addDownloadTasksHelper.addTasks(structureResolver.resolveStructure(*items), configuration)

    override fun removeTask(vararg item: T): Completable =
            removeDownloadTaskHelper.removeTasks(structureResolver.resolveStructure(*item))

    override fun removeTask(vararg id: Long): Completable =
            removeDownloadTaskHelper.removeTasks(structureResolver.resolveStructure(*id))
}