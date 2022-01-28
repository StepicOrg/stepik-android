package org.stepik.android.presentation.course_news.dispatcher

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.course_news.CourseNewsFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseNewsActionDispatcher
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CourseNewsFeature.Action, CourseNewsFeature.Message>() {
    override fun handleAction(action: CourseNewsFeature.Action) {
        // no op
    }
}