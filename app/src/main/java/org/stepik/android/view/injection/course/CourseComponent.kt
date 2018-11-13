package org.stepik.android.view.injection.course

import dagger.Subcomponent
import org.stepic.droid.features.course.ui.activity.CourseActivity
import org.stepic.droid.features.course.ui.fragment.CourseContentFragment
import org.stepic.droid.features.course.ui.fragment.CourseInfoFragment

@CourseScope
@Subcomponent
interface CourseComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseComponent
    }

    fun inject(courseActivity: CourseActivity)
    fun inject(courseInfoFragment: CourseInfoFragment)
    fun inject(courseContentFragment: CourseContentFragment)
}