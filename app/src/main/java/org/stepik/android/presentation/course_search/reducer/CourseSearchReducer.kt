package org.stepik.android.presentation.course_search.reducer

import org.stepik.android.domain.course_search.analytic.CourseContentSearchResultClicked
import org.stepik.android.domain.course_search.analytic.CourseContentSearchedAnalyticEvent
import org.stepik.android.presentation.course_search.CourseSearchFeature.State
import org.stepik.android.presentation.course_search.CourseSearchFeature.Message
import org.stepik.android.presentation.course_search.CourseSearchFeature.Action
import org.stepik.android.presentation.course_search.mapper.CourseSearchResultItemsMapper
import ru.nobird.android.core.model.PagedList
import ru.nobird.android.core.model.concatWithPagedList
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseSearchReducer
@Inject
constructor(
    private val courseSearchResultItemsMapper: CourseSearchResultItemsMapper
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.FetchCourseSearchResultsInitial -> {
                val (newState, action) = State.Loading to setOf(Action.FetchCourseSearchResults(message.courseId, message.courseTitle, message.query, message.isSuggestion))
                when (state) {
                    is State.Loading ->
                        null

                    is State.Content -> {
                        if (state.query != message.query) {
                            newState to action
                        } else {
                            null
                        }
                    }

                    else -> {
                        newState to action
                    }
                }
            }
            is Message.FetchCourseSearchResultsSuccess -> {
                when (state) {
                    is State.Loading -> {
                        val courseSearchState =
                            if (message.courseSearchResultsDataItems.isEmpty()) {
                                State.Empty
                            } else {
                                State.Content(message.courseSearchResultsDataItems, query = message.query, isLoadingNextPage = false, isSuggestion = message.isSuggestion)
                            }
                        courseSearchState to emptySet()
                    }
                    is State.Content -> {
                        val updatedItems = courseSearchResultItemsMapper.updateCourseSearchResults(state.courseSearchResultListDataItems, message.courseSearchResultsDataItems)
                        state.copy(query = message.query, courseSearchResultListDataItems = updatedItems, isLoadingNextPage = false) to emptySet()
                    }
                    else ->
                        null
                }
            }
            is Message.FetchCourseSearchResultsFailure -> {
                if (state is State.Loading || state is State.Content) {
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
                                        message.courseTitle,
                                        message.query,
                                        state.isSuggestion,
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
                            state.copy(courseSearchResultListDataItems = state.courseSearchResultListDataItems.concatWithPagedList(message.courseSearchResultsDataItems))
                        } else {
                            val updatedItems = courseSearchResultItemsMapper.updateCourseSearchResults(state.courseSearchResultListDataItems, message.courseSearchResultsDataItems)
                            state.copy(courseSearchResultListDataItems = updatedItems, isLoadingNextPage = false)
                        }
                    newState to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchCourseSearchResultsNextFailure -> {
                if (state is State.Content) {
                    val newState =
                        if (state.courseSearchResultListDataItems.page < message.page) {
                            val oldLoadedItems = PagedList(
                                list = state.courseSearchResultListDataItems.filter { it.courseSearchResult.progress != null },
                                page = state.courseSearchResultListDataItems.page,
                                hasNext = state.courseSearchResultListDataItems.hasNext,
                                hasPrev = state.courseSearchResultListDataItems.hasPrev
                            )
                            if (oldLoadedItems.isNotEmpty()) {
                                state.copy(courseSearchResultListDataItems = oldLoadedItems, isLoadingNextPage = false)
                            } else {
                                State.Error
                            }
                        } else {
                            val oldLoadedItems = PagedList(
                                list = state.courseSearchResultListDataItems.filter { it.courseSearchResult.progress != null },
                                page = state.courseSearchResultListDataItems.page - 1,
                                hasNext = state.courseSearchResultListDataItems.hasNext,
                                hasPrev = state.courseSearchResultListDataItems.hasPrev
                            )
                            state.copy(courseSearchResultListDataItems = oldLoadedItems, isLoadingNextPage = false)
                        }
                    newState to emptySet()
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
            is Message.CourseContentSearchResultClickedEventMessage -> {
                if (state is State.Content) {
                    val suggestion =
                        if (state.isSuggestion) {
                            message.query
                        } else {
                            null
                        }
                    val event = CourseContentSearchResultClicked(message.courseId, message.courseTitle, message.query, suggestion, message.type, message.step)
                    state to setOf(Action.LogAnalyticEvent(event))
                } else {
                    null
                }
            }
            is Message.CourseContentSearchedEventMessage -> {
                val suggestion =
                    if (message.isSuggestion) {
                        message.query
                    } else {
                        null
                    }
                val event = CourseContentSearchedAnalyticEvent(message.courseId, message.courseTitle, message.query, suggestion)
                state to setOf(Action.LogAnalyticEvent(event))
            }
        } ?: state to emptySet()
}