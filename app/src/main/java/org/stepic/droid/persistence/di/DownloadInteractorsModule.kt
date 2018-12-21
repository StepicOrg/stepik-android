package org.stepic.droid.persistence.di

import dagger.Module
import dagger.Provides
import org.stepic.droid.persistence.downloads.helpers.AddDownloadTaskHelper
import org.stepic.droid.persistence.downloads.helpers.RemoveDownloadTaskHelper
import org.stepic.droid.persistence.downloads.interactor.*
import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepik.android.domain.network.repository.NetworkTypeRepository
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

@Module
abstract class DownloadInteractorsModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PersistenceScope
        fun provideSectionDownloadInteractor(
                structureResolver: StructureResolver<Section>,
                addDownloadTasksHelper: AddDownloadTaskHelper,
                removeDownloadTaskHelper: RemoveDownloadTaskHelper,
                networkTypeRepository: NetworkTypeRepository
        ): DownloadInteractor<Section> =
                DownloadInteractorBase(structureResolver, addDownloadTasksHelper, removeDownloadTaskHelper, networkTypeRepository)

        @JvmStatic
        @Provides
        @PersistenceScope
        fun provideUnitDownloadInteractor(
                structureResolver: StructureResolver<Unit>,
                addDownloadTasksHelper: AddDownloadTaskHelper,
                removeDownloadTaskHelper: RemoveDownloadTaskHelper,
                networkTypeRepository: NetworkTypeRepository
        ): DownloadInteractor<Unit> =
                DownloadInteractorBase(structureResolver, addDownloadTasksHelper, removeDownloadTaskHelper, networkTypeRepository)
    }
    
}