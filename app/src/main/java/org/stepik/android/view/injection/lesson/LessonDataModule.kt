package org.stepik.android.view.injection.lesson

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.lesson.LessonCacheDataSourceImpl
import org.stepik.android.data.lesson.repository.LessonRepositoryImpl
import org.stepik.android.data.lesson.source.LessonCacheDataSource
import org.stepik.android.data.lesson.source.LessonRemoteDataSource
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.remote.lesson.LessonRemoteDataSourceImpl

@Module
abstract class LessonDataModule {
    @Binds
    internal abstract fun bindLessonRepository(
        lessonRepositoryImpl: LessonRepositoryImpl
    ): LessonRepository

    @Binds
    internal abstract fun bindUnitCacheDataSource(
        lessonCacheDataSourceImpl: LessonCacheDataSourceImpl
    ): LessonCacheDataSource

    @Binds
    internal abstract fun bindUnitRemoteDataSource(
        lessonRemoteDataSourceImpl: LessonRemoteDataSourceImpl
    ): LessonRemoteDataSource
}