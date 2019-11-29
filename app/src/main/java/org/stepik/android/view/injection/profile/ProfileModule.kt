package org.stepik.android.view.injection.profile

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.model.user.User
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.profile.ProfilePresenter

@Module
abstract class ProfileModule {
    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(ProfilePresenter::class)
    internal abstract fun bindProfilePresenter(profilePresenter: ProfilePresenter): ViewModel

    @Module
    companion object {
        @Provides
        @JvmStatic
        @ProfileScope
        fun provideUserSubject(): BehaviorSubject<User> =
            BehaviorSubject.create()

        @Provides
        @JvmStatic
        @ProfileScope
        fun providesUserObservable(subject: BehaviorSubject<User>, @BackgroundScheduler scheduler: Scheduler): Observable<User> =
            subject.observeOn(scheduler)
    }
}