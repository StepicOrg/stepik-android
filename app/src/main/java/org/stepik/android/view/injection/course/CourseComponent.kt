package org.stepik.android.view.injection.course

import dagger.Subcomponent
import org.stepik.android.view.course.ui.activity.CourseActivity
import org.stepik.android.view.course_content.ui.fragment.CourseContentFragment
import org.stepik.android.view.course_info.ui.fragment.CourseInfoFragment

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