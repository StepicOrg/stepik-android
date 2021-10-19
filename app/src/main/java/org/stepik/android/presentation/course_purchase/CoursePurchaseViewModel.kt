package org.stepik.android.presentation.course_purchase

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class CoursePurchaseViewModel(
    reduxViewContainer: ReduxViewContainer<CoursePurchaseFeature.State, CoursePurchaseFeature.Message, CoursePurchaseFeature.Action.ViewAction>
) : ReduxViewModel<CoursePurchaseFeature.State, CoursePurchaseFeature.Message, CoursePurchaseFeature.Action.ViewAction>(reduxViewContainer)