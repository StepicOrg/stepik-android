package org.stepik.android.view.injection.profile

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.model.user.Profile

@Module
abstract class ProfileBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        fun provideProfileSubject(): PublishSubject<Profile> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        internal fun provideProfileObservable(profileSubject: PublishSubject<Profile>, @BackgroundScheduler scheduler: Scheduler): Observable<Profile> =
            profileSubject.observeOn(scheduler)
    }
}