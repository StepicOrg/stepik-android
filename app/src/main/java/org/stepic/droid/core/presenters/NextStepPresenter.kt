package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.NextStepView
import org.stepic.droid.model.Lesson

class NextStepPresenter : PresenterBase<NextStepView>() {

    /**
     * Last step in lesson can be shown differently
     */
    fun checkStepForLast(stepId: Long, lesson: Lesson) {
        val stepIds = lesson.steps
        if (stepIds != null && stepIds.size != 0) {
            val lastStepId = stepIds[stepIds.size - 1]
            if (lastStepId == stepId) {
                view?.showNextLessonView()
                return
            }
        }

        //if not shown -> check in db
    }

    fun clickNextLesson(currentLesson: Lesson) {

    }
}