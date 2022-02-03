package org.stepik.android.presentation.course_search

import ru.nobird.app.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class CourseSearchViewModel(
    reduxViewContainer: ReduxViewContainer<CourseSearchFeature.State, CourseSearchFeature.Message, CourseSearchFeature.Action.ViewAction>
) : ReduxViewModel<CourseSearchFeature.State, CourseSearchFeature.Message, CourseSearchFeature.Action.ViewAction>(reduxViewContainer)