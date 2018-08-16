package org.stepic.droid.persistence.di

import dagger.Module
import dagger.Provides
import org.stepic.droid.persistence.downloads.interactor.*
import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

@Module
abstract class DownloadInteractorsModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PersistenceScope
        fun provideSectionDownloadInteractor(structureResolver: StructureResolver<Section>, downloadTasksHelper: DownloadTaskHelper): DownloadInteractor<Section> =
                DownloadInteractorBase(structureResolver, downloadTasksHelper)

        @JvmStatic
        @Provides
        @PersistenceScope
        fun provideUnitDownloadInteractor(structureResolver: StructureResolver<Unit>, downloadTasksHelper: DownloadTaskHelper): DownloadInteractor<Unit> =
                DownloadInteractorBase(structureResolver, downloadTasksHelper)
    }
    
}