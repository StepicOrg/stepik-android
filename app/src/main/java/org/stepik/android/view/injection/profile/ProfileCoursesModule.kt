package org.stepik.android.view.injection.profile

import dagger.Binds
import dagger.Module
import org.stepik.android.presentation.profile_courses.ContinueCourseAction
import org.stepik.android.presentation.profile_courses.ContinueCourseActionImpl

@Module
abstract class ProfileCoursesModule {
    @Binds
    internal abstract fun bindContinueCourseAction(continueCourseActionImpl: ContinueCourseActionImpl): ContinueCourseAction
}