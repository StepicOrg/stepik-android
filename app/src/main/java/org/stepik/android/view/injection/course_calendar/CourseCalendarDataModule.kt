package org.stepik.android.view.injection.course_calendar

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.course_calendar.CourseCalendarDataSourceImpl
import org.stepik.android.data.course_calendar.repository.CourseCalendarRepositoryImpl
import org.stepik.android.data.course_calendar.source.CourseCalendarCacheDataSource
import org.stepik.android.domain.course_calendar.repository.CourseCalendarRepository

@Module
abstract class CourseCalendarDataModule {

    @Binds
    internal abstract fun bindCourseCalendarRepository(
        courseCalendarRepositoryImpl: CourseCalendarRepositoryImpl
    ): CourseCalendarRepository

    @Binds
    internal abstract fun bindCourseCalendarCacheDataSource(
        courseCalendarCacheDataSourceImpl: CourseCalendarDataSourceImpl
    ): CourseCalendarCacheDataSource
}