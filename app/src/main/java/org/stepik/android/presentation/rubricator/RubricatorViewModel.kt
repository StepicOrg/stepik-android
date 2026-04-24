package org.stepik.android.presentation.rubricator

import ru.nobird.android.view.redux.viewmodel.ReduxViewModel
import ru.nobird.app.presentation.redux.container.ReduxViewContainer

class RubricatorViewModel(
    reduxViewContainer: ReduxViewContainer<RubricatorFeature.State, RubricatorFeature.Message, RubricatorFeature.Action.ViewAction>
) : ReduxViewModel<RubricatorFeature.State, RubricatorFeature.Message, RubricatorFeature.Action.ViewAction>(reduxViewContainer)