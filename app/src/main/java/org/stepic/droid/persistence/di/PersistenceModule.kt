package org.stepic.droid.persistence.di

import android.app.DownloadManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.downloads.DownloadTaskManagerImpl
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.files.ExternalStorageManagerImpl
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.repository.DownloadsRepository
import org.stepic.droid.persistence.repository.DownloadsRepositoryImpl
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDaoImpl
import java.util.concurrent.TimeUnit

@Module(includes = [
    ContentModule::class,
    DownloadInteractorsModule::class,
    DownloadTaskAdapterModule::class,
    ProgressProvidersModule::class
])
abstract class PersistenceModule {

    @Binds
    @PersistenceScope
    abstract fun bindUpdatesObservable(subject: PublishSubject<PersistentItem>): Observable<PersistentItem>

    @Binds
    @PersistenceScope
    abstract fun bindUpdatesObserver(subject: PublishSubject<PersistentItem>): Observer<PersistentItem>

    @Binds
    @PersistenceScope
    abstract fun bindSystemDonwloadsDao(systemDownloadsDaoImpl: SystemDownloadsDaoImpl): SystemDownloadsDao

    @Binds
    @PersistenceScope
    abstract fun bindDownloadsRepository(downloadsRepositoryImpl: DownloadsRepositoryImpl): DownloadsRepository

    @Binds
    @PersistenceScope
    abstract fun bindExternalStorageManager(externalStorageManagerImpl: ExternalStorageManagerImpl): ExternalStorageManager

    @Binds
    @PersistenceScope
    abstract fun bindDownloadTaskManager(downloadTaskManagerImpl: DownloadTaskManagerImpl): DownloadTaskManager

    @Module
    companion object {
        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideUpdatesPublishSubject(): PublishSubject<PersistentItem> =
                PublishSubject.create()

        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideIntervalUpdatesObservable(): Observable<kotlin.Unit> =
                Observable.interval(500, TimeUnit.MILLISECONDS).map { kotlin.Unit }.share()

        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideDownloadManager(context: Context): DownloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

}