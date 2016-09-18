package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.CourseDetailModule
import org.stepic.droid.ui.fragments.CourseDetailFragment


@PerFragment
@Subcomponent(modules = arrayOf(CourseDetailModule::class))
interface CourseDetailComponent {
    fun inject(courseDetailFragment: CourseDetailFragment)
}