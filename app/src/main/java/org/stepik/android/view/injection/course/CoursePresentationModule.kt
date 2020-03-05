package org.stepik.android.view.injection.course

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.course.CourseView
import org.stepik.android.presentation.course_continue.CourseContinueView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CoursePresentationModule {
    @Binds
    @IntoMap
    @ViewModelKey(CoursePresenter::class)
    internal abstract fun bindCoursePresenter(coursePresenter: CoursePresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CoursePresentationScope viewContainer: PresenterViewContainer<CourseView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CoursePresentationScope
        fun provideViewContainer(): PresenterViewContainer<CourseView> =
            DefaultPresenterViewContainer()
    }
}