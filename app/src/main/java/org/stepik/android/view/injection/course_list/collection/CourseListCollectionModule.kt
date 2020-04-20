package org.stepik.android.view.injection.course_list.collection

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListCollectionView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListCollectionModule {
    @Binds
    @IntoMap
    @ViewModelKey(CourseListCollectionPresenter::class)
    internal abstract fun bindCourseListCollectionPresenter(courseListCollectionPresenter: CourseListCollectionPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@CourseListCollectionScope viewContainer: PresenterViewContainer<CourseListCollectionView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseListCollectionScope
        fun provideCollectionViewContainer(): PresenterViewContainer<CourseListCollectionView> =
            DefaultPresenterViewContainer()
    }
}