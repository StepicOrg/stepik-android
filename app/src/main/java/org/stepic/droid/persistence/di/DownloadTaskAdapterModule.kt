package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.persistence.downloads.adapters.*
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

@Module
interface DownloadTaskAdapterModule {

    @Binds
    @PersistenceScope
    fun bindSectionDownloadTaskAdapter(sectionDownloadTaskAdapter: SectionDownloadTaskAdapter): DownloadTaskAdapter<Section>

    @Binds
    @PersistenceScope
    fun bindUnitDownloadTaskAdapterImpl(unitDownloadTaskAdapterImpl: UnitDownloadTaskAdapterImpl): UnitDownloadTaskAdapter

    @Binds
    @PersistenceScope
    fun bindUnitDownloadTaskAdapter(unitDownloadTaskAdapter: UnitDownloadTaskAdapter): DownloadTaskAdapter<Unit>

    @Binds
    @PersistenceScope
    fun bindStepDownloadTaskAdapterImpl(stepDownloadTaskAdapterImpl: StepDownloadTaskAdapterImpl): StepDownloadTaskAdapter

}