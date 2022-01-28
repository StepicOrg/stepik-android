package org.stepik.android.view.injection.course

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_news.CourseNewsFeature
import org.stepik.android.presentation.course_news.CourseNewsViewModel
import org.stepik.android.presentation.course_news.dispatcher.CourseNewsActionDispatcher
import org.stepik.android.presentation.course_news.reducer.CourseNewsReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object CourseNewsPresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(CourseNewsViewModel::class)
    internal fun provideCourseNewsPresenter(
        courseNewsReducer: CourseNewsReducer,
        courseNewsActionDispatcher: CourseNewsActionDispatcher
    ): ViewModel =
        CourseNewsViewModel(
            ReduxFeature(CourseNewsFeature.State.Idle, courseNewsReducer)
                .wrapWithActionDispatcher(courseNewsActionDispatcher)
                .wrapWithViewContainer()
        )
}