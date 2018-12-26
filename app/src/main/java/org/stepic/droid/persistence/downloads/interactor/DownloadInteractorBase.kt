package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.persistence.downloads.helpers.AddDownloadTaskHelper
import org.stepic.droid.persistence.downloads.helpers.RemoveDownloadTaskHelper
import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepic.droid.persistence.model.*
import org.stepik.android.domain.network.exception.NetworkRequirementsNotSatisfiedException
import org.stepik.android.domain.network.repository.NetworkTypeRepository

class DownloadInteractorBase<T>(
    private val structureResolver: StructureResolver<T>,
    private val addDownloadTasksHelper: AddDownloadTaskHelper,
    private val removeDownloadTaskHelper: RemoveDownloadTaskHelper,

    private val networkTypeRepository: NetworkTypeRepository
): DownloadInteractor<T> {
    override fun addTask(vararg ids: Long, configuration: DownloadConfiguration): Completable =
        getConfiguration(configuration)
            .flatMapCompletable { config ->
                addDownloadTasksHelper
                    .addTasks(structureResolver.resolveStructure(*ids), config)
            }

    override fun addTask(vararg items: T, configuration: DownloadConfiguration): Completable =
        getConfiguration(configuration)
            .flatMapCompletable { config ->
                addDownloadTasksHelper
                    .addTasks(structureResolver.resolveStructure(*items), config)
            }

    private fun getConfiguration(configuration: DownloadConfiguration): Single<DownloadConfiguration> =
        networkTypeRepository
            .getAllowedNetworkTypes()
            .flatMap { allowedNetworkTypes ->
                requireNetwork(configuration.copy(allowedNetworkTypes = allowedNetworkTypes))
            }

    private fun requireNetwork(configuration: DownloadConfiguration): Single<DownloadConfiguration> =
        networkTypeRepository
            .getAvailableNetworkTypes()
            .flatMap { availableNetworkTypes ->
                if (availableNetworkTypes.intersect(configuration.allowedNetworkTypes).isEmpty()) {
                    Single.error(NetworkRequirementsNotSatisfiedException())
                } else {
                    Single.just(configuration)
                }
            }

    override fun removeTask(vararg item: T): Completable =
        removeDownloadTaskHelper.removeTasks(structureResolver.resolveStructure(*item))

    override fun removeTask(vararg id: Long): Completable =
        removeDownloadTaskHelper.removeTasks(structureResolver.resolveStructure(*id))
}