package org.stepik.android.presentation.course_news

import ru.nobird.android.view.redux.viewmodel.ReduxViewModel
import ru.nobird.app.presentation.redux.container.ReduxViewContainer

class CourseNewsViewModel(
    reduxViewContainer: ReduxViewContainer<CourseNewsFeature.State, CourseNewsFeature.Message, CourseNewsFeature.Action.ViewAction>
) : ReduxViewModel<CourseNewsFeature.State, CourseNewsFeature.Message, CourseNewsFeature.Action.ViewAction>(reduxViewContainer)