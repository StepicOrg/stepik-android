package org.stepik.android.presentation.step_source

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.step_source.interactor.StepSourceInteractor
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

    fun changeStepBlockText(stepPersistentWrapper: StepPersistentWrapper, text: String) {
        if (state !is EditStepSourceView.State.StepLoaded) return

        state = EditStepSourceView.State.Loading
        compositeDisposable += stepSourceInteractor
            .changeStepBlockText(stepPersistentWrapper.originalStep, text)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = EditStepSourceView.State.Complete(it) },
                onError = { state = EditStepSourceView.State.StepLoaded; view?.showNetworkError() }
            )
    }

    fun fetchStepContent(stepPersistentWrapper: StepPersistentWrapper) {
        if (state != EditStepSourceView.State.Idle) return

        state = EditStepSourceView.State.Loading
        compositeDisposable += stepSourceInteractor
            .fetchStep(stepPersistentWrapper.step)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = EditStepSourceView.State.StepLoaded
                    view?.setStepWrapperInfo(it)
                },
                onError = {
                    state = EditStepSourceView.State.StepLoaded
                    view?.setStepWrapperInfo(stepPersistentWrapper)
                }
            )
    }
}