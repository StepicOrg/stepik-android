package org.stepik.android.view.injection.visited_courses

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.AppSingleton
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.visited_courses.VisitedCoursesCacheDataSourceImpl
import org.stepik.android.cache.visited_courses.dao.VisitedCourseDao
import org.stepik.android.data.visited_courses.repository.VisitedCoursesRepositoryImpl
import org.stepik.android.data.visited_courses.source.VisitedCoursesCacheDataSource
import org.stepik.android.data.visited_courses.source.VisitedCoursesRemoteDataSource
import org.stepik.android.domain.visited_courses.repository.VisitedCoursesRepository
import org.stepik.android.remote.visited_courses.VisitedCoursesRemoteDataSourceImpl
import org.stepik.android.remote.visited_courses.service.VisitedCourseService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class VisitedCoursesDataModule {
    @Binds
    @AppSingleton
    internal abstract fun bindVisitedCoursesRepository(
        visitedCoursesRepositoryImpl: VisitedCoursesRepositoryImpl
    ): VisitedCoursesRepository

    @Binds
    internal abstract fun bindVisitedCoursesCacheDataSource(
        visitedCoursesCacheDataSourceImpl: VisitedCoursesCacheDataSourceImpl
    ): VisitedCoursesCacheDataSource

    @Binds
    internal abstract fun bindVisitedCoursesRemoteDataSource(
        visitedCoursesRemoteDataSourceImpl: VisitedCoursesRemoteDataSourceImpl
    ): VisitedCoursesRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideVisitedCourseDao(appDatabase: AppDatabase): VisitedCourseDao =
            appDatabase.visitedCourseDao()

        @Provides
        @JvmStatic
        fun provideVisitedCourseService(@Authorized retrofit: Retrofit): VisitedCourseService =
            retrofit.create()
    }
}