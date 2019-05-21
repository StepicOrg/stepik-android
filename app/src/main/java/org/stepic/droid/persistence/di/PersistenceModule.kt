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
import org.stepic.droid.persistence.downloads.helpers.AddDownloadTaskHelper
import org.stepic.droid.persistence.downloads.helpers.AddDownloadTaskHelperImpl
import org.stepic.droid.persistence.downloads.helpers.RemoveDownloadTaskHelper
import org.stepic.droid.persistence.downloads.helpers.RemoveDownloadTaskHelperImpl
import org.stepic.droid.persistence.downloads.resolvers.DownloadTitleResolver
import org.stepic.droid.persistence.downloads.resolvers.DownloadTitleResolverImpl
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.files.ExternalStorageManagerImpl
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.repository.DownloadsRepository
import org.stepic.droid.persistence.repository.DownloadsRepositoryImpl
import org.stepic.droid.persistence.service.FileTransferService
import org.stepic.droid.persistence.storage.PersistentItemObserver
import org.stepic.droid.persistence.storage.PersistentItemObserverImpl
import org.stepic.droid.persistence.storage.PersistentStateManager
import org.stepic.droid.persistence.storage.PersistentStateManagerImpl
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDaoImpl
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.lesson.LessonDataModule
import org.stepik.android.view.injection.network.NetworkDataModule
import org.stepik.android.view.injection.progress.ProgressDataModule
import org.stepik.android.view.injection.section.SectionDataModule
import org.stepik.android.view.injection.step.StepDataModule
import org.stepik.android.view.injection.unit.UnitDataModule
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Module(includes = [
    ContentModule::class,
    DownloadInteractorsModule::class,
    StructureResolversModule::class,
    ProgressProvidersModule::class,

    CourseDataModule::class,
    LessonDataModule::class,
    UnitDataModule::class,
    SectionDataModule::class,
    ProgressDataModule::class,
    StepDataModule::class,

    NetworkDataModule::class
])
abstract class PersistenceModule {

    @Binds
    @PersistenceScope
    abstract fun bindUpdatesObservable(subject: PublishSubject<Structure>): Observable<Structure>

    @Binds
    @PersistenceScope
    abstract fun bindUpdatesObserver(subject: PublishSubject<Structure>): Observer<Structure>

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

    @Binds
    @PersistenceScope
    abstract fun bindPersistentStateManager(persistentStateManagerImpl: PersistentStateManagerImpl): PersistentStateManager

    @Binds
    @PersistenceScope
    abstract fun bindPersistentItemObserver(persistentItemObserverImpl: PersistentItemObserverImpl): PersistentItemObserver

    @Binds
    @PersistenceScope
    abstract fun bindDownloadTitleResolver(downloadTitleResolverImpl: DownloadTitleResolverImpl): DownloadTitleResolver

    @Binds
    @PersistenceScope
    abstract fun bindAddDownloadTasksHelper(addDownloadTasksHelperImpl: AddDownloadTaskHelperImpl): AddDownloadTaskHelper

    @Binds
    @PersistenceScope
    abstract fun bindRemoveDownloadTasksHelper(removeDownloadTasksHelperImpl: RemoveDownloadTaskHelperImpl): RemoveDownloadTaskHelper

    @Module
    companion object {
        private const val UPDATE_INTERVAL_MS = 1000L

        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideFileTransferEventSubject(): PublishSubject<FileTransferService.Event> =
                PublishSubject.create()

        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideUpdatesPublishSubject(): PublishSubject<Structure> =
                PublishSubject.create()

        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideIntervalUpdatesObservable(): Observable<kotlin.Unit> =
                Observable.interval(UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS).map { kotlin.Unit }

        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideDownloadManager(context: Context): DownloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        @Provides
        @JvmStatic
        @FSLock
        @PersistenceScope
        fun provideFSLock(): ReentrantLock =
                ReentrantLock()
    }

}