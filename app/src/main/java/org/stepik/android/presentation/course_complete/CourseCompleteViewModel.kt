package org.stepik.android.presentation.course_complete

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class CourseCompleteViewModel(
    reduxViewContainer: ReduxViewContainer<CourseCompleteFeature.State, CourseCompleteFeature.Message, CourseCompleteFeature.Action.ViewAction>
) : ReduxViewModel<CourseCompleteFeature.State, CourseCompleteFeature.Message, CourseCompleteFeature.Action.ViewAction>(reduxViewContainer)