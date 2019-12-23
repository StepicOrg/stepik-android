package org.stepik.android.domain.step.analytic

import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.util.getStepType
import org.stepik.android.model.Step

fun Analytic.reportStepEvent(eventName: String, amplitudeEventName: String, step: Step) {
    reportEventWithName(eventName, step.getStepType())
    reportAmplitudeEvent(
        amplitudeEventName, mapOf(
            AmplitudeAnalytic.Steps.Params.TYPE to step.getStepType(),
            AmplitudeAnalytic.Steps.Params.NUMBER to step.position,
            AmplitudeAnalytic.Steps.Params.STEP to step.id
        ))
}