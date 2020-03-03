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
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.profile.ProfilePresenter
import org.stepik.android.presentation.profile_achievements.ProfileAchievementsPresenter
import org.stepik.android.presentation.profile_activities.ProfileActivitiesPresenter
import org.stepik.android.presentation.profile_certificates.ProfileCertificatesPresenter
import org.stepik.android.presentation.profile_courses.ProfileCoursesPresenter
import org.stepik.android.presentation.profile_courses.ProfileCoursesView
import org.stepik.android.presentation.profile_detail.ProfileDetailPresenter
import org.stepik.android.presentation.profile_id.ProfileIdPresenter
import org.stepik.android.presentation.profile_links.ProfileLinksPresenter
import org.stepik.android.presentation.profile_notification.ProfileNotificationPresenter
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

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
    @ViewModelKey(ProfileActivitiesPresenter::class)
    internal abstract fun bindProfileActivitiesPresenter(profileActivitiesPresenter: ProfileActivitiesPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileLinksPresenter::class)
    internal abstract fun bindProfileLinksPresenter(profileLinksPresenter: ProfileLinksPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileNotificationPresenter::class)
    internal abstract fun bindProfileNotificationPresenter(profileNotificationPresenter: ProfileNotificationPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileIdPresenter::class)
    internal abstract fun bindProfileIdPresenter(profileIdPresenter: ProfileIdPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileCoursesPresenter::class)
    internal abstract fun bindProfileCoursesPresenter(ProfileCoursesPresenter: ProfileCoursesPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileCertificatesPresenter::class)
    internal abstract fun bindProfileCertificatePresenter(profileCertificatesPresenter: ProfileCertificatesPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@ProfileScope viewContainer: PresenterViewContainer<ProfileCoursesView>): ViewContainer<out CourseContinueView>

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

        @Provides
        @JvmStatic
        @ProfileScope
        fun provideViewContainer(): PresenterViewContainer<ProfileCoursesView> =
            DefaultPresenterViewContainer()
    }
}