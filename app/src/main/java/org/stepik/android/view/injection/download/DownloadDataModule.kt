package org.stepik.android.view.injection.download

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.stepic.droid.persistence.downloads.progress.CourseDownloadProgressProvider
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepik.android.cache.download.DownloadCacheDataSourceImpl
import org.stepik.android.data.download.repository.DownloadRepositoryImpl
import org.stepik.android.data.download.source.DownloadCacheDataSource
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.download.interactor.DownloadInteractor
import org.stepik.android.domain.download.mapper.DownloadProgressStatusMapperImpl
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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideDownloadInteractor(
            externalStorageManager: ExternalStorageManager,
            updatesObservable: Observable<Structure>,
            intervalUpdatesObservable: Observable<Unit>,

            systemDownloadsDao: SystemDownloadsDao,
            persistentItemDao: PersistentItemDao,
            persistentStateManager: PersistentStateManager,
            downloadRepository: DownloadRepository,
            courseRepository: CourseRepository): DownloadInteractor {
            return DownloadInteractor(downloadRepository, courseRepository,
                CourseDownloadProgressProvider(
                    updatesObservable,
                    intervalUpdatesObservable,

                    systemDownloadsDao,
                    persistentItemDao,
                    persistentStateManager,
                    DownloadProgressStatusMapperImpl(externalStorageManager)
                )
            )
        }
    }
}