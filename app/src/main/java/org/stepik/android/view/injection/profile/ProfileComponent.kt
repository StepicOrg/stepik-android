package org.stepik.android.view.injection.profile

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepik.android.view.achievement.ui.fragment.AchievementsListFragment
import org.stepik.android.view.injection.certificate.CertificateDataModule
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.social_profile.SocialProfileDataModule
import org.stepik.android.view.injection.tags.TagsDataModule
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_activity.UserActivityDataModule
import org.stepik.android.view.profile.ui.fragment.ProfileFragment
import org.stepik.android.view.profile_achievements.ui.fragment.ProfileAchievementsFragment
import org.stepik.android.view.profile_activities.ui.fragment.ProfileActivitiesFragment
import org.stepik.android.view.profile_certificates.ui.fragment.ProfileCertificatesFragment
import org.stepik.android.view.profile_detail.ui.fragment.ProfileDetailFragment
import org.stepik.android.view.profile_id.ui.fragment.ProfileIdFragment
import org.stepik.android.view.profile_links.ui.fragment.ProfileLinksFragment
import org.stepik.android.view.profile_notification.ui.fragment.ProfileNotificationFragment

@ProfileScope
@Subcomponent(modules = [
    ProfileModule::class,

    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    ProfileDataModule::class,
    UserDataModule::class,
    UserActivityDataModule::class,
    SocialProfileDataModule::class,
    CertificateDataModule::class,
    TagsDataModule::class
])
interface ProfileComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileComponent

        @BindsInstance
        fun userId(@UserId userId: Long): Builder
    }

    fun profileCoursesPresentationComponentBuilder(): ProfileCoursesComponent.Builder

    fun inject(profileFragment: ProfileFragment)
    fun inject(achievementsListFragment: AchievementsListFragment)
    fun inject(profileAchievementsFragment: ProfileAchievementsFragment)
    fun inject(profileDetailFragment: ProfileDetailFragment)
    fun inject(profileActivitiesFragment: ProfileActivitiesFragment)
    fun inject(profileLinksFragment: ProfileLinksFragment)
    fun inject(profileNotificationFragment: ProfileNotificationFragment)
    fun inject(profileIdFragment: ProfileIdFragment)
    fun inject(profileCertificatesFragment: ProfileCertificatesFragment)
}