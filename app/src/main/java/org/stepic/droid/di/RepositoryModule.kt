package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.model.*
import org.stepik.android.model.structure.Unit
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.storage.repositories.course.CourseRepositoryImpl
import org.stepic.droid.storage.repositories.lesson.LessonRepositoryImpl
import org.stepic.droid.storage.repositories.section.SectionRepositoryImpl
import org.stepic.droid.storage.repositories.step.StepRepositoryImpl
import org.stepic.droid.storage.repositories.unit.UnitRepositoryImpl
import org.stepik.android.model.structure.Course
import org.stepik.android.model.structure.Lesson
import org.stepik.android.model.structure.Section

@Module
interface RepositoryModule {

    @Binds
    fun bindCourseRepository(courseRepositoryImpl: CourseRepositoryImpl): Repository<Course>

    @Binds
    fun bindSectionRepository(sectionRepositoryImpl: SectionRepositoryImpl): Repository<Section>

    @Binds
    fun bindUnitRepository(unitRepositoryImpl: UnitRepositoryImpl): Repository<Unit>

    @Binds
    fun bindLessonRepository(lessonRepositoryImpl: LessonRepositoryImpl): Repository<Lesson>

    @Binds
    fun bindStepRepository(stepRepositoryImpl: StepRepositoryImpl): Repository<Step>

}
