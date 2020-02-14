package org.stepik.android.presentation.step_quiz_code

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.user_code_run.interactor.UserCodeRunInteractor
import org.stepik.android.presentation.base.PresenterBase
import timber.log.Timber
import javax.inject.Inject

class StepQuizCodeRunPresenter
@Inject
constructor(
    private val userCodeRunInteractor: UserCodeRunInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StepQuizRunCode>() {
    private var state: StepQuizRunCode.State = StepQuizRunCode.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: StepQuizRunCode) {
        super.attachView(view)
        view.setState(state)
    }

    fun createUserCodeRun(code: String, language: String, stdin: String, stepId: Long) {
        compositeDisposable += userCodeRunInteractor
            .createUserCodeRun(code, language, stdin, stepId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { Timber.d("Success: $it") },
                onError = { Timber.d("Error: $it")}
            )
    }
}