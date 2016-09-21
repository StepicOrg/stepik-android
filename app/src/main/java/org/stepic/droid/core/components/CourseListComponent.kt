package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.core.modules.CourseListModule
import org.stepic.droid.core.PerFragment
import org.stepic.droid.ui.fragments.CourseListFragmentBase
import org.stepic.droid.ui.fragments.CourseSearchFragment


@PerFragment
@Subcomponent(modules = arrayOf(CourseListModule::class))
interface CourseListComponent {
    fun inject(fragment: CoursesDatabaseFragmentBase)

    fun inject(fragment: CourseListFragmentBase)

    fun inject(fragment: CourseSearchFragment)
}
