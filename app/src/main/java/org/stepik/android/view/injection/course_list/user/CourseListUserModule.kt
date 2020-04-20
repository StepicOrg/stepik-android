package org.stepik.android.view.injection.course_list.user

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListUserPresenter
import org.stepik.android.presentation.course_list.CourseListUserView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListUserModule {
    @Binds
    @IntoMap
    @ViewModelKey(CourseListUserPresenter::class)
    internal abstract fun bindCourseListUserPresenter(courseListUserPresenter: CourseListUserPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CourseListUserScope viewContainer: PresenterViewContainer<CourseListUserView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseListUserScope
        fun provideUserViewContainer(): PresenterViewContainer<CourseListUserView> =
            DefaultPresenterViewContainer()
    }
}