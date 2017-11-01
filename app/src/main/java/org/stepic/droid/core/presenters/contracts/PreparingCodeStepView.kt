package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Step

interface PreparingCodeStepView {
    fun onStepPrepared(newStep : Step)

    fun onStepNotPrepared()

    fun onStepPreparing()
}
