package org.stepik.android.view.injection.course_list

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_list.CourseListPlaygroundPresenter
import org.stepik.android.presentation.course_list.CourseListPlaygroundView
import org.stepik.android.presentation.course_list.CourseListView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class CourseListPlaygroundModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(CourseListPlaygroundPresenter::class)
    internal abstract fun bindCourseListPresenter(courseListPlaygroundPresenter: CourseListPlaygroundPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseListViewContainer(viewContainer: PresenterViewContainer<CourseListPlaygroundView>): ViewContainer<out CourseListView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideViewContainer(): PresenterViewContainer<CourseListPlaygroundView> =
            DefaultPresenterViewContainer()
//
//        @Provides
//        @JvmStatic
//        fun provideStateContainer(): StateContainer<CourseListView.State> =
//            DefaultStateContainer(CourseListView.State.Idle)
    }
}