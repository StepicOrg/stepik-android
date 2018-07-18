package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.stepic.droid.core.presenters.contracts.PreparingCodeStepView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.di.step.code.CodeScope
import org.stepik.android.model.structure.Step
import org.stepic.droid.util.isCodeStepReady
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

    private val compositeDisposable = CompositeDisposable()

    fun prepareStep(step: Step) {
        if (step.isCodeStepReady()) {
            view?.onStepPrepared(step)
            return
        }

        view?.onStepPreparing()
        val disposable = api.getStepsReactive(longArrayOf(step.id))
                .map {
                    val internetStep = it.steps?.first()
                    when (internetStep?.isCodeStepReady()) {
                        true -> internetStep
                        else -> throw StepNotPrepared()
                    }
                }
                .subscribeOn(scheduler)
                .observeOn(mainScheduler)
                .subscribe({ newStep ->
                    view?.onStepPrepared(newStep)
                }, {
                    view?.onStepNotPrepared()
                })
        compositeDisposable.add(disposable)
    }

    override fun detachView(view: PreparingCodeStepView) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    private class StepNotPrepared : Throwable("Step is not prepared")
}
