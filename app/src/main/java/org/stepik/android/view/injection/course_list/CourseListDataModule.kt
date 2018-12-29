package org.stepik.android.view.injection.course_list

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.course_list.CourseListCacheDataSourceImpl
import org.stepik.android.data.course_list.repository.CourseListRepositoryImpl
import org.stepik.android.data.course_list.source.CourseListCacheDataSource
import org.stepik.android.domain.course_list.repository.CourseListRepository

@Module
abstract class CourseListDataModule {

    @Binds
    internal abstract fun bindCourseListRepository(
        courseListRepositoryImpl: CourseListRepositoryImpl
    ): CourseListRepository

    @Binds
    internal abstract fun bindCourseListCacheDataSource(
        courseListCacheDataSourceImpl: CourseListCacheDataSourceImpl
    ): CourseListCacheDataSource

}