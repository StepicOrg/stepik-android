package org.stepik.android.presentation.course_revenue

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class CourseRevenueViewModel(
    reduxViewContainer: ReduxViewContainer<CourseRevenueFeature.State, CourseRevenueFeature.Message, CourseRevenueFeature.Action.ViewAction>
) : ReduxViewModel<CourseRevenueFeature.State, CourseRevenueFeature.Message, CourseRevenueFeature.Action.ViewAction>(reduxViewContainer)