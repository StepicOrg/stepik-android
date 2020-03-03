package org.stepik.android.view.injection.course_list

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_list.CourseListPresenter

@Module
abstract class CourseListModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(CourseListPresenter::class)
    internal abstract fun bindCourseListPresenter(courseListPresenter: CourseListPresenter): ViewModel
}