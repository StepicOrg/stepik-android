package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.Exceptions
import org.stepic.droid.core.presenters.contracts.PreparingCodeStepView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.di.step.code.CodeScope
import org.stepic.droid.model.Step
import org.stepic.droid.model.StepStatus
import org.stepic.droid.util.RetryExponential
import org.stepic.droid.web.Api
import javax.inject.Inject

@CodeScope
class PreparingCodeStepPresenter
@Inject
constructor(
        private val api: Api,
        @BackgroundScheduler
        private val scheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler) : PresenterBase<PreparingCodeStepView>() {

    companion object {
        private const val ATTEMPTS = 10
    }

    private val compositeDisposable = CompositeDisposable()

    fun checkStep(step: Step) {
        if (step.isCodeStepPrepared()) {
            view?.onStepPrepared()
        } else {
            view?.onStepNotPrepared()
        }
    }

    fun prepareStepIfNotPrepared(step: Step) {
        if (step.isCodeStepPrepared()) {
            view?.onStepPrepared()
            return
        }

        val disposable = api.getStepsReactive(longArrayOf(step.id))
                .map {
                    it.steps?.first()
                }
                .map {
                    when (it.isCodeStepPrepared()) {
                        true -> it
                        false -> Exceptions.propagate(StepNotPrepared())
                    }
                }
                .retryWhen(RetryExponential(ATTEMPTS))
                .subscribeOn(scheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    view?.onStepPrepared()
                }, {
                    view?.onStepNotPrepared()
                })
        compositeDisposable.add(disposable)
    }

    override fun detachView(view: PreparingCodeStepView) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    private fun Step.isCodeStepPrepared() =
            this.block?.options != null && this.status != StepStatus.PREPARING

    private class StepNotPrepared : Throwable("Step is not prepared")
}
