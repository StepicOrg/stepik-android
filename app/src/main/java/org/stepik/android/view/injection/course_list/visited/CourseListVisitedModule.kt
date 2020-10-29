package org.stepik.android.view.injection.course_list.visited

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.presentation.course_list.CourseListVisitedPresenter
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListVisitedModule {
    @Binds
    @IntoMap
    @ViewModelKey(CourseListVisitedPresenter::class)
    internal abstract fun bindCourseListVisitedPresenter(courseListVisitedPresenter: CourseListVisitedPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CourseListVisitedScope viewContainer: PresenterViewContainer<CourseListView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseListVisitedScope
        fun provideVisitedViewContainer(): PresenterViewContainer<CourseListView> =
            DefaultPresenterViewContainer()
    }
}