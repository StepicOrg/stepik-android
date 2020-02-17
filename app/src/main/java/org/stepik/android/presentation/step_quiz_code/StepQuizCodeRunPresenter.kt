package org.stepik.android.presentation.step_quiz_code

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.user_code_run.interactor.UserCodeRunInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class StepQuizCodeRunPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val userCodeRunInteractor: UserCodeRunInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StepQuizRunCodeView>() {
    private var state: StepQuizRunCodeView.State = StepQuizRunCodeView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: StepQuizRunCodeView) {
        super.attachView(view)
        view.setState(state)
    }

    fun createUserCodeRun(code: String, language: String, stdin: String, stepId: Long) {
        if (!(state == StepQuizRunCodeView.State.Idle || state is StepQuizRunCodeView.State.UserCodeRunLoaded)) {
            return
        }
        state = if (state == StepQuizRunCodeView.State.Idle) {
            StepQuizRunCodeView.State.Loading
        } else {
            val userCodeRun = (state as StepQuizRunCodeView.State.UserCodeRunLoaded).userCodeRun
            StepQuizRunCodeView.State.ConsequentLoading(userCodeRun)
        }
        compositeDisposable += userCodeRunInteractor
            .createUserCodeRun(code, language, stdin, stepId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.RunCode.RUN_CODE_LAUNCHED, mapOf(AmplitudeAnalytic.RunCode.Params.STEP_ID to it.step))
                    state = StepQuizRunCodeView.State.UserCodeRunLoaded(it)
                },
                onError = {
                    view?.showNetworkError()
                    state = if (state is StepQuizRunCodeView.State.ConsequentLoading) {
                        val userCodeRun = (state as StepQuizRunCodeView.State.ConsequentLoading).userCodeRun
                        StepQuizRunCodeView.State.UserCodeRunLoaded(userCodeRun)
                    } else {
                        StepQuizRunCodeView.State.Idle
                    }
                }
            )
    }

    fun resolveRunCodePopup() {
        compositeDisposable += userCodeRunInteractor
            .isRunCodePopupShown()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { isRunCodePopupShown ->
                    if (!isRunCodePopupShown) {
                        view?.showRunCodePopup()
                    }
                },
                onError = emptyOnErrorStub
            )
    }
}