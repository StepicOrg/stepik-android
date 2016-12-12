package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Section
import org.stepic.droid.model.Step
import org.stepic.droid.model.Unit

interface ContinueCourseView {

    fun onShowContinueCourseLoadingDialog()

    fun onOpenStep(section: Section, unit: Unit, step: Step)

    fun onConnectionProblemWhileContinue()
}