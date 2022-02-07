package org.stepik.android.view.injection.course_complete

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_complete.CourseCompleteFeature
import org.stepik.android.presentation.course_complete.CourseCompleteViewModel
import org.stepik.android.presentation.course_complete.dispatcher.CourseCompleteActionDispatcher
import org.stepik.android.presentation.course_complete.reducer.CourseCompleteReducer
import ru.nobird.app.presentation.redux.container.wrapWithViewContainer
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.ReduxFeature

@Module
object CourseCompletePresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(CourseCompleteViewModel::class)
    internal fun provideCourseCompletePresenter(
        courseCompleteReducer: CourseCompleteReducer,
        courseCompleteActionDispatcher: CourseCompleteActionDispatcher
    ): ViewModel =
        CourseCompleteViewModel(
            ReduxFeature(CourseCompleteFeature.State.Idle, courseCompleteReducer)
                .wrapWithActionDispatcher(courseCompleteActionDispatcher)
                .wrapWithViewContainer()
        )
}