package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.NextStepView
import org.stepic.droid.model.Lesson

class NextStepPresenter : PresenterBase<NextStepView>() {

    /**
     * Last step in lesson can be shown differently
     */
    fun checkStepForLast(stepId: Long, lesson: Lesson) {

    }
}