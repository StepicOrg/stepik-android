package org.stepik.android.presentation.course_news

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class CourseNewsViewModel(
    reduxViewContainer: ReduxViewContainer<CourseNewsFeature.State, CourseNewsFeature.Message, CourseNewsFeature.Action.ViewAction>
) : ReduxViewModel<CourseNewsFeature.State, CourseNewsFeature.Message, CourseNewsFeature.Action.ViewAction>(reduxViewContainer)