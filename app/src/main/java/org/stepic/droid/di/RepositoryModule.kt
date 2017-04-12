package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.model.Unit
import org.stepic.droid.storage.repositories.IRepository
import org.stepic.droid.storage.repositories.course.CourseRepositoryImpl
import org.stepic.droid.storage.repositories.section.SectionRepositoryImpl
import org.stepic.droid.storage.repositories.unit.UnitRepositoryImpl

@Module
interface RepositoryModule {

    @Binds
    fun bindCourseRepository(courseRepositoryImpl: CourseRepositoryImpl): IRepository<Course, Long>

    @Binds
    fun bindSectionRepository(sectionRepositoryImpl: SectionRepositoryImpl): IRepository<Section, Long>

    @Binds
    fun bindUnitRepository(unitRepositoryImpl: UnitRepositoryImpl): IRepository<Unit, Long>

}
