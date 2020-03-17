package org.stepik.android.view.injection.course_list

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.presentation.course_list.CourseListView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListModule {
    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(CourseListPresenter::class)
    internal abstract fun bindCourseListPresenter(courseListPresenter: CourseListPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CourseListScope viewContainer: PresenterViewContainer<CourseListView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseListScope
        fun provideViewContainer(): PresenterViewContainer<CourseListView> =
            DefaultPresenterViewContainer()
    }
}