package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.LessonTrackingView
import org.stepic.droid.di.lesson.LessonScope
import org.stepic.droid.model.Step
import org.stepic.droid.util.getStepType
import javax.inject.Inject

@LessonScope
class StepsTrackingPresenter
@Inject constructor(
        private val analytic: Analytic) : PresenterBase<LessonTrackingView>() {

    fun trackStepType(step: Step) {
        analytic.reportEventWithName(Analytic.Steps.STEP_OPENED, step.block?.name ?: "null_type")
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Steps.STEP_OPENED, mapOf(
                AmplitudeAnalytic.Steps.Params.TYPE to step.getStepType(),
                AmplitudeAnalytic.Steps.Params.NUMBER to step.position,
                AmplitudeAnalytic.Steps.Params.STEP to step.id
        ))
    }
}
