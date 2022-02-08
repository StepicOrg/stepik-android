package org.stepik.android.presentation.course_news.reducer

import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_news.exception.NotEnrolledException
import org.stepik.android.presentation.course_news.CourseNewsFeature.State
import org.stepik.android.presentation.course_news.CourseNewsFeature.Message
import org.stepik.android.presentation.course_news.CourseNewsFeature.Action
import ru.nobird.app.core.model.slice
import ru.nobird.app.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseNewsReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    companion object {
        private const val PAGE_SIZE = 20
    }
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                val sourceType = if (state is State.Idle && !state.mustFetchRemote) {
                    DataSourceType.CACHE
                } else {
                    DataSourceType.REMOTE
                }

                val fetchAnnouncementIds = message.announcementIds.slice(0, PAGE_SIZE)

                if (fetchAnnouncementIds.isEmpty()) {
                    State.Empty(message.announcementIds) to emptySet()
                } else {
                    State.LoadingAnnouncements(message.announcementIds, sourceType) to setOf(Action.FetchAnnouncements(fetchAnnouncementIds, sourceType))
                }
            }
            is Message.OnScreenOpenedMessage -> {
                when (state) {
                    /**
                     * Necessary for the case, when CourseActivity is launched with News tab
                     */
                    is State.Idle -> {
                        State.Idle(mustFetchRemote = true) to emptySet()
                    }
                    is State.Empty -> {
                        State.LoadingAnnouncements(state.announcementIds, DataSourceType.REMOTE) to
                            setOf(Action.FetchAnnouncements(state.announcementIds.slice(0, PAGE_SIZE), DataSourceType.REMOTE))
                    }
                    is State.LoadingAnnouncements -> {
                        if (state.sourceType == DataSourceType.CACHE) {
                            State.LoadingAnnouncements(state.announcementIds, DataSourceType.REMOTE) to
                                setOf(Action.FetchAnnouncements(state.announcementIds.slice(0, PAGE_SIZE), DataSourceType.REMOTE))
                        } else {
                            null
                        }
                    }
                    is State.Content -> {
                        if (state.sourceType == DataSourceType.CACHE) {
                            state.copy(isLoadingRemote = true) to
                                setOf(Action.FetchAnnouncements(state.announcementIds.slice(0, PAGE_SIZE), DataSourceType.REMOTE))
                        } else {
                            null
                        }
                    }
                    else ->
                        null
                }
            }
            is Message.FetchAnnouncementIdsFailure -> {
                if (state is State.Idle) {
                    if (message.throwable is NotEnrolledException) {
                        State.NotEnrolled to emptySet()
                    } else {
                        State.Error to emptySet()
                    }
                } else {
                    null
                }
            }
            is Message.FetchCourseNewsSuccess -> {
                when (state) {
                    is State.LoadingAnnouncements -> {
                        if (message.courseNewsListItems.isEmpty()) {
                            State.Empty(state.announcementIds) to emptySet()
                        } else {
                            State.Content(
                                state.announcementIds,
                                message.courseNewsListItems,
                                state.sourceType,
                                isLoadingRemote = false,
                                isLoadingNextPage = false
                            ) to emptySet()
                        }
                    }
                    is State.Content -> {
                        state.copy(
                            courseNewsListItems = message.courseNewsListItems,
                            sourceType = DataSourceType.REMOTE,
                            isLoadingRemote = false
                        ) to emptySet()
                    }
                    else ->
                        null
                }
            }
            is Message.FetchCourseNewsFailure -> {
                if (state is State.LoadingAnnouncements) {
                    if (state.sourceType == DataSourceType.REMOTE) {
                        state.copy(sourceType = DataSourceType.CACHE) to
                            setOf(Action.FetchAnnouncements(state.announcementIds.slice(0, PAGE_SIZE), DataSourceType.CACHE))
                    } else {
                        State.Error to emptySet()
                    }
                } else {
                    null
                }
            }
            is Message.FetchNextPage -> {
                if (state is State.Content && !state.isLoadingRemote && !state.isLoadingNextPage) {
                    val offset = state.courseNewsListItems.size
                    val fetchAnnouncementIds = state.announcementIds.slice(offset, offset + PAGE_SIZE)
                    if (fetchAnnouncementIds.isNotEmpty()) {
                        state.copy(isLoadingNextPage = true) to setOf(Action.FetchAnnouncements(fetchAnnouncementIds, state.sourceType, isNextPage = true))
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            is Message.FetchCourseNewsNextPageSuccess -> {
                if (state is State.Content) {
                    state.copy(courseNewsListItems = state.courseNewsListItems + message.courseNewsListItems, isLoadingNextPage = false) to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchCourseNewsNextPageFailure -> {
                if (state is State.Content) {
                    state.copy(isLoadingNextPage = false) to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}