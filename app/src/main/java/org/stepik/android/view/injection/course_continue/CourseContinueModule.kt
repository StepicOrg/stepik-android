package org.stepik.android.view.injection.course_continue

import dagger.Binds
import dagger.Module
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl

@Module
abstract class CourseContinueModule {
    @Binds
    internal abstract fun bindContinueCoursePresenterDelegate(continueCoursePresenterDelegateImpl: CourseContinuePresenterDelegateImpl): CourseContinuePresenterDelegate
}