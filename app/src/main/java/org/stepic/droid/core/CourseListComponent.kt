package org.stepic.droid.core

import dagger.Subcomponent
import org.stepic.droid.base.CoursesDatabaseFragmentBase


@PerFragment
@Subcomponent(modules = arrayOf(CourseListModule::class))
interface CourseListComponent {
    fun inject(fragment: CoursesDatabaseFragmentBase)
}
