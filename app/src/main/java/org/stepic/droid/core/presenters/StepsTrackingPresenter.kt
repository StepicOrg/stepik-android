package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.LessonTrackingView
import org.stepic.droid.di.step.StepScope
import org.stepic.droid.di.lesson.LessonScope
import org.stepic.droid.model.Step
import javax.inject.Inject

@LessonScope
class StepsTrackingPresenter
@Inject constructor(
        private val analytic: Analytic) : PresenterBase<LessonTrackingView>() {

    fun trackStepType(step: Step) {
        analytic.reportEventWithName(Analytic.Steps.STEP_OPENED, step.block?.name ?: "null_type")
    }
}
