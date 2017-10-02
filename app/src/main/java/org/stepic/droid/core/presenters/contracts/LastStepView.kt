package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Step

interface LastStepView {

    fun onShowLastStep(step: Step)

    fun onShowPlaceholder()
}
