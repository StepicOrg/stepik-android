package org.stepik.android.view.injection.download

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.download.DownloadCacheDataSourceImpl
import org.stepik.android.data.download.repository.DownloadRepositoryImpl
import org.stepik.android.data.download.source.DownloadCacheDataSource
import org.stepik.android.domain.download.repository.DownloadRepository

@Module
abstract class DownloadDataModule {
    @Binds
    internal abstract fun bindDownloadRepository(
        downloadRepositoryImpl: DownloadRepositoryImpl
    ): DownloadRepository

    @Binds
    internal abstract fun bindDownloadCacheDataSource(
        downloadCacheDataSourceImpl: DownloadCacheDataSourceImpl
    ): DownloadCacheDataSource
}