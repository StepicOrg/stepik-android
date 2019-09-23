package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.persistence.downloads.resolvers.structure.CourseStructureResolverImpl
import org.stepic.droid.persistence.downloads.resolvers.structure.SectionStructureResolver
import org.stepic.droid.persistence.downloads.resolvers.structure.StepStructureResolver
import org.stepic.droid.persistence.downloads.resolvers.structure.StepStructureResolverImpl
import org.stepic.droid.persistence.downloads.resolvers.structure.StructureResolver
import org.stepic.droid.persistence.downloads.resolvers.structure.UnitStructureResolver
import org.stepic.droid.persistence.downloads.resolvers.structure.UnitStructureResolverImpl
import org.stepik.android.model.Course
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

    @Binds
    @PersistenceScope
    fun bindCourseStructureResolver(courseStructureResolverImpl: CourseStructureResolverImpl): StructureResolver<Course>

}