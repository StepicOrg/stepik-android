package org.stepik.android.presentation.course_complete.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_complete.interactor.CourseCompleteInteractor
import org.stepik.android.presentation.course_complete.CourseCompleteFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseCompleteActionDispatcher
@Inject
constructor(
    private val courseCompleteInteractor: CourseCompleteInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseCompleteFeature.Action, CourseCompleteFeature.Message>() {
    override fun handleAction(action: CourseCompleteFeature.Action) {
        when (action) {
            is CourseCompleteFeature.Action.FetchCourseCompleteInfo -> {
                compositeDisposable += courseCompleteInteractor
                    .getCourseCompleteInfo(action.course)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CourseCompleteFeature.Message.FetchCourseCompleteInfoSuccess(it)) },
                        onError = { onNewMessage(CourseCompleteFeature.Message.FetchCourseCompleteError) }
                    )
            }
        }
    }
}