package org.stepik.android.view.injection.exam_session

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.exam_session.ExamSessionCacheDataSourceImpl
import org.stepik.android.cache.exam_session.dao.ExamSessionDao
import org.stepik.android.data.exam_session.repository.ExamSessionRepositoryImpl
import org.stepik.android.data.exam_session.source.ExamSessionCacheDataSource
import org.stepik.android.data.exam_session.source.ExamSessionRemoteDataSource
import org.stepik.android.domain.exam_session.repository.ExamSessionRepository
import org.stepik.android.remote.exam_session.ExamSessionRemoteDataSourceImpl
import org.stepik.android.remote.exam_session.service.ExamSessionService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class ExamSessionDataModule {
    @Binds
    internal abstract fun bindExamSessionRepository(
        examSessionRepositoryImpl: ExamSessionRepositoryImpl
    ): ExamSessionRepository

    @Binds
    internal abstract fun bindExamSessionCacheDataSource(
        examSessionCacheDataSourceImpl: ExamSessionCacheDataSourceImpl
    ): ExamSessionCacheDataSource

    @Binds
    internal abstract fun bindExamSessionRemoteDataSource(
        examSessionRemoteDataSourceImpl: ExamSessionRemoteDataSourceImpl
    ): ExamSessionRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideExamSessionDao(appDatabase: AppDatabase): ExamSessionDao =
            appDatabase.examSessionDao()

        @Provides
        @JvmStatic
        fun provideExamSessionService(@Authorized retrofit: Retrofit): ExamSessionService =
            retrofit.create()
    }
}