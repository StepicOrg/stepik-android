package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.PreparingCodeStepView
import org.stepic.droid.di.step.code.CodeScope
import org.stepic.droid.model.Step
import org.stepic.droid.model.StepStatus
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CodeScope
class PreparingCodeStepPresenter
@Inject
constructor(
        private val api: Api,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler) : PresenterBase<PreparingCodeStepView>() {

    fun prepareStepIfNotPrepared(step: Step) {
        if (step.block?.options != null && step.status != StepStatus.PREPARING) {
            view?.onStepPrepared()
        } else {

        }

    }
}
