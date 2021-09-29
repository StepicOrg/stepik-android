package org.stepik.android.presentation.course_search.reducer

import org.stepik.android.presentation.course_search.CourseSearchFeature.State
import org.stepik.android.presentation.course_search.CourseSearchFeature.Message
import org.stepik.android.presentation.course_search.CourseSearchFeature.Action
import org.stepik.android.presentation.course_search.mapper.CourseSearchStateMapper
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseSearchReducer
@Inject
constructor(
    private val courseSearchStateMapper: CourseSearchStateMapper
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.FetchCourseSearchResultsInitial -> {
                if (state !is State.Loading) {
                    State.Loading to setOf(Action.FetchCourseSearchResults(message.courseId, message.query))
                } else {
                    null
                }
            }
            is Message.FetchCourseSearchResultsSuccess -> {
                when (state) {
                    is State.Loading -> {
                        val courseSearchState =
                            if (message.courseSearchResultsDataItems.isEmpty()) {
                                State.Empty
                            } else {
                                State.Content(message.courseSearchResultsDataItems, isLoadingNextPage = false)
                            }
                        courseSearchState to emptySet()
                    }
                    is State.Content -> {
                        courseSearchStateMapper.updateCourseSearchResults(state, message.courseSearchResultsDataItems) to emptySet()
                    }
                    else ->
                        null
                }
            }
            is Message.FetchCourseSearchResultsFailure -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else
                    null
            }
            is Message.FetchNextPage -> {
                if (state is State.Content) {
                    if (state.courseSearchResultListDataItems.hasNext && !state.isLoadingNextPage) {
                        state.copy(isLoadingNextPage = true) to
                                setOf(
                                    Action.FetchCourseSearchResults(
                                        message.courseId,
                                        message.query,
                                        state.courseSearchResultListDataItems.page + 1
                                    )
                                )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            is Message.FetchCourseSearchResultsNextSuccess -> {
                if (state is State.Content) {
                    val newState =
                        if (state.courseSearchResultListDataItems.page < message.courseSearchResultsDataItems.page) {
                            courseSearchStateMapper.concatCourseSearchResults(state, message.courseSearchResultsDataItems)
                        } else {
                            courseSearchStateMapper.updateCourseSearchResults(state, message.courseSearchResultsDataItems)
                        }
                    newState to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchCourseSearchResultsNextFailure -> {
                if (state is State.Content) {
                    state.copy(isLoadingNextPage = false) to emptySet()
                } else {
                    null
                }
            }
            is Message.InitDiscussionThreadMessage -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.ShowLoadingDialog, Action.FetchDiscussionThread(message.step, message.discussionId))
                } else {
                    null
                }
            }
            is Message.DiscussionThreadSuccess -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.HideLoadingDialog, Action.ViewAction.OpenComment(message.step, message.discussionThread, message.discussionId))
                } else {
                    null
                }
            }
            is Message.DiscussionThreadError -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.HideLoadingDialog)
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}