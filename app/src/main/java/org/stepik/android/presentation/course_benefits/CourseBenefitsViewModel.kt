package org.stepik.android.presentation.course_benefits

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class CourseBenefitsViewModel(
    reduxViewContainer: ReduxViewContainer<CourseBenefitsFeature.State, CourseBenefitsFeature.Message, CourseBenefitsFeature.Action.ViewAction>
) : ReduxViewModel<CourseBenefitsFeature.State, CourseBenefitsFeature.Message, CourseBenefitsFeature.Action.ViewAction>(reduxViewContainer)