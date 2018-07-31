package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDao
import org.stepic.droid.persistence.storage.dao.SystemDownloadsDaoImpl

@Module
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

    @Module
    companion object {
        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideUpdatesPublishSubject(): PublishSubject<PersistentItem> =
                PublishSubject.create()

//        @Provides // todo: remove from AppCoreModule
//        @JvmStatic
//        @PersistenceScope
//        fun provideDownloadManager(context: Context): DownloadManager =
//                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

}