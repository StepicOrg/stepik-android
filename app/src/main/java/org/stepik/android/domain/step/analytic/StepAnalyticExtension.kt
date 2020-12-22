package org.stepik.android.domain.step.analytic

import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.util.getStepType
import org.stepik.android.model.Step

fun Analytic.reportStepEvent(amplitudeEventName: String, step: Step) {
    reportAmplitudeEvent(
        amplitudeEventName, mapOf(
            AmplitudeAnalytic.Steps.Params.TYPE to step.getStepType(),
            AmplitudeAnalytic.Steps.Params.NUMBER to step.position,
            AmplitudeAnalytic.Steps.Params.STEP to step.id,
            AmplitudeAnalytic.Steps.Params.IS_REVIEW to (step.actions?.doReview != null)
        ))
}