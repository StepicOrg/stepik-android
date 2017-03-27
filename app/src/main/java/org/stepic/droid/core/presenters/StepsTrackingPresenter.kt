package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.StepsTrackingView
import org.stepic.droid.model.Step

class StepsTrackingPresenter(private val analytic: Analytic) : PresenterBase<StepsTrackingView>() {

    fun trackStepType(step: Step) {
        analytic.reportEventWithName(Analytic.Steps.STEP_OPENED, step.block?.name ?: "null_type")
    }
}
