package org.stepik.android.presentation.course_news.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_news.exception.NotEnrolledException
import org.stepik.android.domain.course_news.interactor.CourseNewsInteractor
import org.stepik.android.presentation.course_news.CourseNewsFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseNewsActionDispatcher
@Inject
constructor(
    private val courseNewsInteractor: CourseNewsInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseNewsFeature.Action, CourseNewsFeature.Message>() {
    init {
        subscribeCourseUpdates()
    }

    override fun handleAction(action: CourseNewsFeature.Action) {
        when (action) {
            is CourseNewsFeature.Action.FetchAnnouncements -> {
                compositeDisposable += courseNewsInteractor
                    .getAnnouncements(action.announcementIds, action.isTeacher, action.sourceType)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { courseNewsListItems ->
                            if (action.isNextPage) {
                                onNewMessage(CourseNewsFeature.Message.FetchCourseNewsNextPageSuccess(courseNewsListItems))
                            } else {
                                onNewMessage(CourseNewsFeature.Message.FetchCourseNewsSuccess(courseNewsListItems))
                            }
                        },
                        onError = {
                            if (action.isNextPage) {
                                onNewMessage(CourseNewsFeature.Message.FetchCourseNewsNextPageFailure)
                            } else {
                                onNewMessage(CourseNewsFeature.Message.FetchCourseNewsFailure)
                            }
                        }
                    )
            }
        }
    }

    private fun subscribeCourseUpdates() {
        compositeDisposable += courseNewsInteractor
            .getCourse()
            .map { course ->
                if (course.enrollment != 0L) {
                    Result.success(course)
                } else {
                    Result.failure(NotEnrolledException())
                }
            }
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { result ->
                    result.fold(
                        onSuccess = { course ->
                            onNewMessage(
                                CourseNewsFeature.Message.InitMessage(
                                    course.announcements?.sortedDescending() ?: emptyList(),
                                    course.actions?.createAnnouncements != null
                                )
                            )
                        },
                        onFailure = { onNewMessage(CourseNewsFeature.Message.FetchAnnouncementIdsFailure(it)) }
                    )
                },
                onError = { onNewMessage(CourseNewsFeature.Message.FetchAnnouncementIdsFailure(it)); subscribeCourseUpdates(); }
            )
    }
}