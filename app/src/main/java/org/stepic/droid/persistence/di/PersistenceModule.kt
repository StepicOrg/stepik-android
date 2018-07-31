package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.persistence.model.PersistentItem

@Module
abstract class PersistenceModule {

    @Binds
    @PersistenceScope
    abstract fun bindUpdatesObservable(subject: PublishSubject<PersistentItem>): Observable<PersistentItem>

    @Binds
    @PersistenceScope
    abstract fun bindUpdatesObserver(subject: PublishSubject<PersistentItem>): Observer<PersistentItem>

    companion object {
        @Provides
        @JvmStatic
        @PersistenceScope
        fun provideUpdatesPublishSubject(): PublishSubject<PersistentItem> =
                PublishSubject.create()
    }

}