package org.stepik.android.view.injection.course_list

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.course_list.CourseListCacheDataSourceImpl
import org.stepik.android.data.course_list.repository.CourseListRepositoryImpl
import org.stepik.android.data.course_list.source.CourseListCacheDataSource
import org.stepik.android.data.course_list.source.CourseListRemoteDataSource
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.remote.course_list.CourseListRemoteDataSourceImpl
import org.stepik.android.remote.course_list.service.CourseListService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Binds
    internal abstract fun bindCourseListRemoteDataSource(
        courseListRemoteDataSourceImpl: CourseListRemoteDataSourceImpl
    ): CourseListRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCourseListService(@Authorized retrofit: Retrofit): CourseListService =
            retrofit.create(CourseListService::class.java)
    }
}