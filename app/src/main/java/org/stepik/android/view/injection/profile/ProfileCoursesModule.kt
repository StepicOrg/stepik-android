package org.stepik.android.view.injection.profile

import dagger.Binds
import dagger.Module
import org.stepik.android.presentation.course_continue.delegate.ContinueCoursePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.ContinueCoursePresenterDelegateImpl

@Module
abstract class ProfileCoursesModule {
    @Binds
    internal abstract fun bindContinueCoursePresenterDelegate(continueCoursePresenterDelegateImpl: ContinueCoursePresenterDelegateImpl): ContinueCoursePresenterDelegate
}