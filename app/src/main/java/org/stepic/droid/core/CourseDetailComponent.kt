package org.stepic.droid.core

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CourseDetailFragment


@PerFragment
@Subcomponent(modules = arrayOf(CourseDetailModule::class))
interface CourseDetailComponent {
    fun inject(courseDetailFragment: CourseDetailFragment)
}