package org.stepik.android.presentation.lesson_demo.dispatcher

import org.stepik.android.domain.course.repository.CoursePurchaseDataRepository
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class LessonDemoActionDispatcher
@Inject
constructor(
    private val coursePurchaseDataRepository: CoursePurchaseDataRepository
) : RxActionDispatcher<LessonDemoFeature.Action, LessonDemoFeature.Message>() {
    override fun handleAction(action: LessonDemoFeature.Action) {
        // no op
    }
}