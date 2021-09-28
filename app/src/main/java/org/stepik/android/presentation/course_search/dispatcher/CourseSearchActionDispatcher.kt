package org.stepik.android.presentation.course_search.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_search.interactor.CourseSearchInteractor
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.presentation.course_search.CourseSearchFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseSearchActionDispatcher
@Inject
constructor(
    private val courseSearchInteractor: CourseSearchInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseSearchFeature.Action, CourseSearchFeature.Message>() {
    override fun handleAction(action: CourseSearchFeature.Action) {
        when (action) {
            is CourseSearchFeature.Action.FetchCourseSearchResults -> {
                val searchResultQuery = SearchResultQuery(page = action.page, query = action.query, course = action.courseId)
                compositeDisposable += courseSearchInteractor
                    .addSearchQuery(action.courseId, searchResultQuery)
                    .andThen(courseSearchInteractor.getCourseSearchResult(searchResultQuery))
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onNext = {
                            val message =
                                if (searchResultQuery.page == 1) {
                                    CourseSearchFeature.Message.FetchCourseSearchResultsSuccess(it)
                                } else {
                                    CourseSearchFeature.Message.FetchCourseSearchResultsNextSuccess(it)
                                }
                            onNewMessage(message)
                        },
                        onError = { onNewMessage(CourseSearchFeature.Message.FetchCourseSearchResultsFailure) }
                    )
            }
            is CourseSearchFeature.Action.FetchDiscussionThread -> {
                compositeDisposable += courseSearchInteractor
                    .getDiscussionThreads(action.step)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { discussionThreads ->
                            val discussionThread = discussionThreads.find { it.thread == DiscussionThread.THREAD_DEFAULT }
                            val message =
                                if (discussionThread != null) {
                                    CourseSearchFeature.Message.DiscussionThreadSuccess(action.step, discussionThread, action.discussionId)
                                } else {
                                    CourseSearchFeature.Message.DiscussionThreadError
                                }
                            onNewMessage(message)
                        },
                        onError = {
                            onNewMessage(CourseSearchFeature.Message.DiscussionThreadError)
                        }
                    )
            }
        }
    }
}