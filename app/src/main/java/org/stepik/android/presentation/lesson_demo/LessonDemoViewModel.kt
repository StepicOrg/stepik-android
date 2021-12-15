package org.stepik.android.presentation.lesson_demo

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class LessonDemoViewModel(
    reduxViewContainer: ReduxViewContainer<LessonDemoFeature.State, LessonDemoFeature.Message, LessonDemoFeature.Action.ViewAction>
) : ReduxViewModel<LessonDemoFeature.State, LessonDemoFeature.Message, LessonDemoFeature.Action.ViewAction>(reduxViewContainer)