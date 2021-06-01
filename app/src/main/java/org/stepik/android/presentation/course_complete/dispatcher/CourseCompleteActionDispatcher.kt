package org.stepik.android.presentation.course_complete.dispatcher

import org.stepik.android.domain.course_complete.interactor.CourseCompleteInteractor
import org.stepik.android.presentation.course_complete.CourseCompleteFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CourseCompleteActionDispatcher
@Inject
constructor(
    private val courseCompleteInteractor: CourseCompleteInteractor
) : RxActionDispatcher<CourseCompleteFeature.Action, CourseCompleteFeature.Message>() {
    override fun handleAction(action: CourseCompleteFeature.Action) {
        TODO("Not yet implemented")
    }
}