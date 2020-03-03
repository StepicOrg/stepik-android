package org.stepik.android.view.injection.course_list

import dagger.Binds
import dagger.Module
import org.stepik.android.presentation.course_list.delegate.CourseListPresenterDelegate
import org.stepik.android.presentation.course_list.delegate.CourseListPresenterDelegateImpl

@Module
abstract class CourseListModule {
    @Binds
    internal abstract fun bindCourseListPresenterDelegate(courseListPresenterDelegateImpl: CourseListPresenterDelegateImpl): CourseListPresenterDelegate
}