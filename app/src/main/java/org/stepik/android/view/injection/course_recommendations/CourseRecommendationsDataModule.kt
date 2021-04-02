package org.stepik.android.view.injection.course_recommendations

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.course_recommendations.CourseRecommendationsCacheDataSourceImpl
import org.stepik.android.cache.course_recommendations.dao.CourseRecommendationsDao
import org.stepik.android.data.course_recommendations.repository.CourseRecommendationsRepositoryImpl
import org.stepik.android.data.course_recommendations.source.CourseRecommendationsCacheDataSource
import org.stepik.android.data.course_recommendations.source.CourseRecommendationsRemoteDataSource
import org.stepik.android.domain.course_recommendations.repository.CourseRecommendationsRepository
import org.stepik.android.remote.course_recommendations.CourseRecommendationsRemoteDataSourceImpl
import org.stepik.android.remote.course_recommendations.service.CourseRecommendationsService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class CourseRecommendationsDataModule {
    @Binds
    internal abstract fun bindCourseRecommendationsRepository(
        courseRecommendationsRepository: CourseRecommendationsRepositoryImpl
    ): CourseRecommendationsRepository

    @Binds
    internal abstract fun bindCourseRecommendationsCacheDataSource(
        courseRecommendationsCacheDataSourceImpl: CourseRecommendationsCacheDataSourceImpl
    ): CourseRecommendationsCacheDataSource

    @Binds
    internal abstract fun bindCourseRecommendationsRemoteDataSource(
        courseRecommendationsRemoteDataSourceImpl: CourseRecommendationsRemoteDataSourceImpl
    ): CourseRecommendationsRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideCourseRecommendationsDao(appDatabase: AppDatabase): CourseRecommendationsDao =
            appDatabase.courseRecommendationsDao()

        @Provides
        @JvmStatic
        internal fun provideCourseRecommendationsService(@Authorized retrofit: Retrofit): CourseRecommendationsService =
            retrofit.create()
    }
}