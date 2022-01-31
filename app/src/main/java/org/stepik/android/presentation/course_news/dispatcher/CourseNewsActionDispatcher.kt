package org.stepik.android.presentation.course_news.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
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
        compositeDisposable += courseNewsInteractor
            .getCourse()
            .map { course -> course.announcements?.sortedDescending() ?: emptyList() }
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { announcementIds -> onNewMessage(CourseNewsFeature.Message.InitMessage(announcementIds)) },
                onError = { onNewMessage(CourseNewsFeature.Message.FetchAnnouncementIdsFailure) }
            )
    }
    override fun handleAction(action: CourseNewsFeature.Action) {
        when (action) {
            is CourseNewsFeature.Action.FetchAnnouncements -> {
                compositeDisposable += courseNewsInteractor
                    .getAnnouncements(action.announcementIds, action.sourceType)
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
}