package org.stepik.android.view.injection.course_search

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_search.CourseSearchFeature
import org.stepik.android.presentation.course_search.CourseSearchViewModel
import org.stepik.android.presentation.course_search.dispatcher.CourseSearchActionDispatcher
import org.stepik.android.presentation.course_search.reducer.CourseSearchReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object CourseSearchPresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(CourseSearchViewModel::class)
    internal fun provideCourseSearchPresenter(
        courseSearchReducer: CourseSearchReducer,
        courseSearchActionDispatcher: CourseSearchActionDispatcher
    ): ViewModel =
        CourseSearchViewModel(
            ReduxFeature(CourseSearchFeature.State.Idle, courseSearchReducer)
                .wrapWithActionDispatcher(courseSearchActionDispatcher)
                .wrapWithViewContainer()
        )
}