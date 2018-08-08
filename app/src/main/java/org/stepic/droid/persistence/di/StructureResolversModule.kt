package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.persistence.downloads.adapters.*
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

@Module
interface StructureResolversModule {

    @Binds
    @PersistenceScope
    fun bindSectionStructureResolver(sectionStructureResolver: SectionStructureResolver): StructureResolver<Section>

    @Binds
    @PersistenceScope
    fun bindUnitStructureResolverImpl(unitStructureResolverImpl: UnitStructureResolverImpl): UnitStructureResolver

    @Binds
    @PersistenceScope
    fun bindUnitStructureResolver(unitStructureResolver: UnitStructureResolver): StructureResolver<Unit>

    @Binds
    @PersistenceScope
    fun bindStepStructureResolverImpl(stepStructureResolverImpl: StepStructureResolverImpl): StepStructureResolver

}