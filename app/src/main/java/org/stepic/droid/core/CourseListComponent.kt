package org.stepic.droid.core

import dagger.Subcomponent
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.ui.fragments.CourseListFragmentBase
import org.stepic.droid.ui.fragments.CourseSearchFragment


@PerFragment
@Subcomponent(modules = arrayOf(CourseListModule::class))
interface CourseListComponent {
    fun inject(fragment: CoursesDatabaseFragmentBase)

    fun inject(fragment: CourseListFragmentBase)

    fun inject(fragment: CourseSearchFragment)
}
