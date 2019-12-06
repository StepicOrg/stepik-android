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
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.profile.ProfilePresenter
import org.stepik.android.presentation.profile_achievements.ProfileAchievementsPresenter
import org.stepik.android.presentation.profile_detail.ProfileDetailPresenter
import org.stepik.android.presentation.profile_links.ProfileLinksPresenter

@Module
abstract class ProfileModule {
    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(ProfilePresenter::class)
    internal abstract fun bindProfilePresenter(profilePresenter: ProfilePresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileDetailPresenter::class)
    internal abstract fun bindProfileDetailPresenter(profileDetailPresenter: ProfileDetailPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileAchievementsPresenter::class)
    internal abstract fun bindAchievementsPresenter(profileAchievementsPresenter: ProfileAchievementsPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileLinksPresenter::class)
    internal abstract fun bindProfileLinksPresenter(profileLinksPresenter: ProfileLinksPresenter): ViewModel

    @Module
    companion object {
        @Provides
        @JvmStatic
        @ProfileScope
        fun provideUserSubject(): BehaviorSubject<ProfileData> =
            BehaviorSubject.create()

        @Provides
        @JvmStatic
        @ProfileScope
        fun providesUserObservable(subject: BehaviorSubject<ProfileData>, @BackgroundScheduler scheduler: Scheduler): Observable<ProfileData> =
            subject.observeOn(scheduler)
    }
}