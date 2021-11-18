package org.stepik.android.view.injection.profile

import dagger.Subcomponent
import org.stepik.android.view.injection.mobile_tiers.MobileTiersDataModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule
import org.stepik.android.view.profile_courses.ui.fragment.ProfileCoursesFragment

@ProfileCoursesScope
@Subcomponent(modules = [ProfileCoursesPresentationModule::class, WishlistDataModule::class, MobileTiersDataModule::class])
interface ProfileCoursesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileCoursesComponent
    }
    fun inject(profileCoursesFragment: ProfileCoursesFragment)
}