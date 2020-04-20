package org.stepik.android.view.injection.course_list.query

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.presentation.course_list.CourseListQueryView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListQueryModule {
    @Binds
    @IntoMap
    @ViewModelKey(CourseListQueryPresenter::class)
    internal abstract fun bindCourseListPresenter(courseListQueryPresenter: CourseListQueryPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CourseListQueryScope viewContainer: PresenterViewContainer<CourseListQueryView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseListQueryScope
        fun provideQueryViewContainer(): PresenterViewContainer<CourseListQueryView> =
            DefaultPresenterViewContainer()
    }
}