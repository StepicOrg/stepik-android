package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.persistence.downloads.progress.DownloadProgressProvider
import org.stepic.droid.persistence.downloads.progress.SectionDownloadProgressProvider
import org.stepic.droid.persistence.downloads.progress.UnitDownloadProgressProvider
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

@Module
interface ProgressProvidersModule {

    @Binds
    @PersistenceScope
    fun bindSectionDownloadProgressProvider(sectionDownloadProgressProvider: SectionDownloadProgressProvider): DownloadProgressProvider<Section>

    @Binds
    @PersistenceScope
    fun bindUnitDownloadProgressProvider(unitDownloadProgressProvider: UnitDownloadProgressProvider): DownloadProgressProvider<Unit>

}