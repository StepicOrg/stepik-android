package org.stepik.android.view.story.viewmodel

import org.stepik.android.presentation.story.StoryFeature
import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class StoryViewModel(
    reduxViewContainer: ReduxViewContainer<StoryFeature.State, StoryFeature.Message, StoryFeature.Action.ViewAction>
) : ReduxViewModel<StoryFeature.State, StoryFeature.Message, StoryFeature.Action.ViewAction>(reduxViewContainer)