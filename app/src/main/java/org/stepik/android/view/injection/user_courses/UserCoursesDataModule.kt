package org.stepik.android.view.injection.user_courses

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.user_courses.UserCoursesCacheDataSourceImpl
import org.stepik.android.data.user_courses.repository.UserCoursesRepositoryImpl
import org.stepik.android.data.user_courses.source.UserCoursesCacheDataSource
import org.stepik.android.data.user_courses.source.UserCoursesRemoteDataSource
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.remote.user_courses.UserCoursesRemoteDataSourceImpl
import org.stepik.android.remote.user_courses.service.UserCoursesService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class UserCoursesDataModule {
    @Binds
    internal abstract fun bindUserCoursesRepository(
        userCoursesRepositoryImpl: UserCoursesRepositoryImpl
    ): UserCoursesRepository

    @Binds
    internal abstract fun bindUserCoursesRemoteDataSource(
        userCoursesRemoteDataSourceImpl: UserCoursesRemoteDataSourceImpl
    ): UserCoursesRemoteDataSource

    @Binds
    internal abstract fun bindUserCoursesCacheDataSource(
        userCoursesCacheDataSourceImpl: UserCoursesCacheDataSourceImpl
    ): UserCoursesCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideUserCoursesService(@Authorized retrofit: Retrofit): UserCoursesService =
            retrofit.create(UserCoursesService::class.java)
    }
}