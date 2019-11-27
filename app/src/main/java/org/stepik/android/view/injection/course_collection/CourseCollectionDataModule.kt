package org.stepik.android.view.injection.course_collection

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.course_collection.repository.CourseCollectionRepositoryImpl
import org.stepik.android.data.course_collection.source.CourseCollectionRemoteDataSource
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.remote.course_collection.CourseCollectionRemoteDataSourceImpl
import org.stepik.android.remote.course_collection.service.CourseCollectionService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class CourseCollectionDataModule {
    @Binds
    internal abstract fun bindCourseCollectionRepository(
        courseCollectionRepositoryImpl: CourseCollectionRepositoryImpl
    ): CourseCollectionRepository

    @Binds
    internal abstract fun bindCourseCollectionRemoteDataSource(
        courseCollectionRemoteDataSourceImpl: CourseCollectionRemoteDataSourceImpl
    ): CourseCollectionRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCourseCollectionService(@Authorized retrofit: Retrofit): CourseCollectionService =
            retrofit.create(CourseCollectionService::class.java)
    }
}