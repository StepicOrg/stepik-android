package org.stepik.android.view.injection.lesson

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.lesson.LessonCacheDataSourceImpl
import org.stepik.android.data.lesson.repository.LessonRepositoryImpl
import org.stepik.android.data.lesson.source.LessonCacheDataSource
import org.stepik.android.data.lesson.source.LessonRemoteDataSource
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.remote.lesson.LessonRemoteDataSourceImpl
import org.stepik.android.remote.lesson.service.LessonService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class LessonDataModule {
    @Binds
    internal abstract fun bindLessonRepository(
        lessonRepositoryImpl: LessonRepositoryImpl
    ): LessonRepository

    @Binds
    internal abstract fun bindLessonCacheDataSource(
        lessonCacheDataSourceImpl: LessonCacheDataSourceImpl
    ): LessonCacheDataSource

    @Binds
    internal abstract fun bindLessonRemoteDataSource(
        lessonRemoteDataSourceImpl: LessonRemoteDataSourceImpl
    ): LessonRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideLessonService(@Authorized retrofit: Retrofit): LessonService =
            retrofit.create(LessonService::class.java)
    }
}