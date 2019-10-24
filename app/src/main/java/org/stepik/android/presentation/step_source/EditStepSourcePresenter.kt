package org.stepik.android.presentation.step_source

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.step_source.interactor.StepSourceInteractor
import org.stepik.android.model.Step
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class EditStepSourcePresenter
@Inject
constructor(
    private val stepSourceInteractor: StepSourceInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<EditStepSourceView>() {
    private var state: EditStepSourceView.State = EditStepSourceView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: EditStepSourceView) {
        super.attachView(view)
        view.setState(state)
    }

    fun changeStepBlockText(step: Step, text: String) {
        if (state != EditStepSourceView.State.Idle) return

        state = EditStepSourceView.State.Loading
        compositeDisposable += stepSourceInteractor
            .changeStepBlockText(step, text)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = EditStepSourceView.State.Complete(it) },
                onError = { state = EditStepSourceView.State.Idle; view?.showNetworkError() }
            )
    }
}