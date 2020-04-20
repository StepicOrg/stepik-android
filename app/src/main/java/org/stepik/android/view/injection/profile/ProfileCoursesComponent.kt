package org.stepik.android.view.injection.profile

import dagger.Subcomponent
import org.stepik.android.view.profile_courses.ui.fragment.ProfileCoursesFragment

@ProfileCoursesScope
@Subcomponent(modules = [ProfileCoursesPresentationModule::class])
interface ProfileCoursesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileCoursesComponent
    }
    fun inject(profileCoursesFragment: ProfileCoursesFragment)
}