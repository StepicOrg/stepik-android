package org.stepik.android.view.injection.proctor_session

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.proctor_session.ProctorSessionCacheDataSourceImpl
import org.stepik.android.cache.proctor_session.dao.ProctorSessionDao
import org.stepik.android.data.proctor_session.repository.ProctorSessionRepositoryImpl
import org.stepik.android.data.proctor_session.source.ProctorSessionCacheDataSource
import org.stepik.android.data.proctor_session.source.ProctorSessionRemoteDataSource
import org.stepik.android.domain.proctor_session.repository.ProctorSessionRepository
import org.stepik.android.remote.proctor_session.ProctorSessionRemoteDataSourceImpl
import org.stepik.android.remote.proctor_session.service.ProctorSessionService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class ProctorSessionDataModule {
    @Binds
    internal abstract fun bindProctorSessionRepository(
        proctorSessionRepositoryImpl: ProctorSessionRepositoryImpl
    ): ProctorSessionRepository

    @Binds
    internal abstract fun bindProctorSessionCacheDataSource(
        proctorSessionCacheDataSourceImpl: ProctorSessionCacheDataSourceImpl
    ): ProctorSessionCacheDataSource

    @Binds
    internal abstract fun bindProctorSessionRemoteDataSource(
        proctorSessionRemoteDataSourceImpl: ProctorSessionRemoteDataSourceImpl
    ): ProctorSessionRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideProctorSessionDao(appDatabase: AppDatabase): ProctorSessionDao =
            appDatabase.proctorSessionDao()

        @Provides
        @JvmStatic
        fun provideProctorSessionService(@Authorized retrofit: Retrofit): ProctorSessionService =
            retrofit.create()
    }
}