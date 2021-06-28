package org.stepik.android.view.injection.course_list.wishlist

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListWishPresenter
import org.stepik.android.presentation.course_list.CourseListWishView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListWishModule {
    @Binds
    @IntoMap
    @ViewModelKey(CourseListWishPresenter::class)
    internal abstract fun bindCourseListWishPresenter(courseListWishPresenter: CourseListWishPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CourseListWishScope viewContainer: PresenterViewContainer<CourseListWishView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseListWishScope
        fun provideCollectionViewContainer(): PresenterViewContainer<CourseListWishView> =
            DefaultPresenterViewContainer()
    }
}