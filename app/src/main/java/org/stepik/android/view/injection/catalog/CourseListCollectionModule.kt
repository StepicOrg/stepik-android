package org.stepik.android.view.injection.catalog

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

// todo only sample
@Module
abstract class CourseListCollectionModule {
    @Binds
    @IntoMap
    @ViewModelKey(CourseListCollectionPresenter::class)
    internal abstract fun bindCourseListCollectionPresenter(courseListCollectionPresenter: CourseListCollectionPresenter): ViewModel

//    @Binds
//    @IntoMap
//    @ViewModelKey(CourseListQueryPresenter::class)
//    internal abstract fun bindCourseListPresenter(courseListQueryPresenter: CourseListQueryPresenter): ViewModel

    @Module
    companion object {
        private var pvc: PresenterViewContainer<CourseListCollectionView>? = null

        @Provides
        @JvmStatic
        fun provideViewContainer(): PresenterViewContainer<CourseListCollectionView> =
            pvc ?: DefaultPresenterViewContainer<CourseListCollectionView>().also { pvc = it }

        @Provides
        @JvmStatic
        fun bindCourseContinueViewContainer(): ViewContainer<out CourseContinueView> {
            val vc = requireNotNull(pvc)
            pvc = null
            return vc
        }
    }
}