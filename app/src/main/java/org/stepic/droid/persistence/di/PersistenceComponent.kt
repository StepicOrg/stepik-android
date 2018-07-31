package org.stepic.droid.persistence.di

import dagger.Subcomponent
import org.stepic.droid.persistence.service.DownloadCompleteService

@PersistenceScope
@Subcomponent(modules = [PersistenceModule::class])
interface PersistenceComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): PersistenceComponent
    }

    fun inject(downloadCompleteService: DownloadCompleteService)
}