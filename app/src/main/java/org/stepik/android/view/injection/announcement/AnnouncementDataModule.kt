package org.stepik.android.view.injection.announcement

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.announcement.AnnouncementCacheDataSourceImpl
import org.stepik.android.cache.announcement.dao.AnnouncementDao
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.data.announcement.repository.AnnouncementRepositoryImpl
import org.stepik.android.data.announcement.source.AnnouncementCacheDataSource
import org.stepik.android.data.announcement.source.AnnouncementRemoteDataSource
import org.stepik.android.domain.announcement.repository.AnnouncementRepository
import org.stepik.android.remote.announcement.AnnouncementRemoteDataSourceImpl
import org.stepik.android.remote.announcement.service.AnnouncementService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class AnnouncementDataModule {
    @Binds
    internal abstract fun bindAnnouncementRepository(
        announcementRepositoryImpl: AnnouncementRepositoryImpl
    ): AnnouncementRepository

    @Binds
    internal abstract fun bindAnnouncementRemoteDataSource(
        announcementRemoteDataSourceImpl: AnnouncementRemoteDataSourceImpl
    ): AnnouncementRemoteDataSource

    @Binds
    internal abstract fun bindAnnouncementCacheDataSource(
        announcementCacheDataSourceImpl: AnnouncementCacheDataSourceImpl
    ): AnnouncementCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideAnnouncementDao(appDatabase: AppDatabase): AnnouncementDao =
            appDatabase.announcementDao()

        @Provides
        @JvmStatic
        internal fun provideAnnouncementService(@Authorized retrofit: Retrofit): AnnouncementService =
            retrofit.create()
    }
}