package org.stepik.android.view.injection.course_list.search_result

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListSearchPresenter
import org.stepik.android.presentation.course_list.CourseListSearchResultView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListSearchResultModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(CourseListSearchPresenter::class)
    internal abstract fun bindCourseListSearchPresenter(courseListSearchPresenter: CourseListSearchPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CourseListSearchResultScope viewContainer: PresenterViewContainer<CourseListSearchResultView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseListSearchResultScope
        fun provideViewContainer(): PresenterViewContainer<CourseListSearchResultView> =
            DefaultPresenterViewContainer()
    }
}