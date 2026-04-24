package org.stepik.android.presentation.rubricator

import org.stepik.android.domain.rubricator.model.CourseList
import org.stepik.android.domain.rubricator.model.MetaCategory
import org.stepik.android.presentation.rubricator.RubricatorFeature.State
import org.stepik.android.presentation.rubricator.RubricatorFeature.Message
import org.stepik.android.presentation.rubricator.RubricatorFeature.Action

import ru.nobird.app.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class RubricatorReducer @Inject constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state.rubricatorState is RubricatorFeature.RubricatorState.Idle) {
                    state.copy(rubricatorState = RubricatorFeature.RubricatorState.Loading) to setOf(
                        Action.FetchRubricatorData(message.rubricatorUrl)
                    )
                } else {
                    null
                }
            }

            is Message.FetchRubricatorDataSuccess -> {
                if (state.rubricatorState is RubricatorFeature.RubricatorState.Loading) {
                    state.copy(rubricatorState = RubricatorFeature.RubricatorState.Content(message.rubricatorData)) to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchRubricatorDataError -> {
                if (state.rubricatorState is RubricatorFeature.RubricatorState.Loading) {
                    state.copy(rubricatorState = RubricatorFeature.RubricatorState.Error) to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchMetaCategories -> {
                if (state.rubricatorState is RubricatorFeature.RubricatorState.Content) {
                    val allMetaCategories = state
                        .rubricatorState
                        .rubricatorData
                        .metaCategories
                        .associateBy(MetaCategory::id)

                    val subjectTitle = message.subject.title
                    val metaCategories = message.subject.metaCategories.mapNotNull { allMetaCategories[it] }
                    state.copy(metaCategoryState = RubricatorFeature.MetaCategoryState.Content(subjectTitle, metaCategories)) to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchCourseLists -> {
                if (state.rubricatorState is RubricatorFeature.RubricatorState.Content) {
                    val allCourseLists = state
                        .rubricatorState
                        .rubricatorData
                        .courseLists
                        .associateBy(CourseList::id)

                    val metaCategoryTitle = message.metaCategory.title
                    val courseLists = message.metaCategory.courseLists.mapNotNull { allCourseLists[it] }
                    state.copy(courseListState = RubricatorFeature.CourseListState.Content(metaCategoryTitle, courseLists)) to emptySet()
                } else {
                    null
                }
            }

            is Message.OnBackPressed -> {
                when (state.rubricatorState) {
                    RubricatorFeature.RubricatorState.Idle,
                    RubricatorFeature.RubricatorState.Loading,
                    RubricatorFeature.RubricatorState.Error -> state to setOf(Action.ViewAction.CloseScreen)
                    is RubricatorFeature.RubricatorState.Content -> {
                        when {
                            state.courseListState is RubricatorFeature.CourseListState.Content ->
                                state.copy(courseListState = RubricatorFeature.CourseListState.Empty) to emptySet()
                            state.metaCategoryState is RubricatorFeature.MetaCategoryState.Content ->
                                state.copy(
                                    metaCategoryState = RubricatorFeature.MetaCategoryState.Empty,
                                    courseListState = RubricatorFeature.CourseListState.Empty
                                    ) to emptySet()
                            else -> state to setOf(Action.ViewAction.CloseScreen)
                        }
                    }
                }
            }
        } ?: (state to emptySet())
}