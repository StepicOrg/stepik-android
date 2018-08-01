package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.persistence.downloads.interactor.DownloadInteractor
import org.stepic.droid.persistence.downloads.interactor.SectionDownloadInteractor
import org.stepic.droid.persistence.downloads.interactor.UnitDownloadInteractor
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

@Module
interface DownloadInteractorsModule {
    @Binds
    fun bindUnitDownloadInteractor(unitDownloadInteractor: UnitDownloadInteractor): DownloadInteractor<Unit>

    @Binds
    fun bindSectionDownloadInteractor(sectionDownloadInteractor: SectionDownloadInteractor): DownloadInteractor<Section>
}