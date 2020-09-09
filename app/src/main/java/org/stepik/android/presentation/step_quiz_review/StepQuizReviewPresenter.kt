package org.stepik.android.presentation.step_quiz_review

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.step_quiz_review.interactor.StepQuizReviewInteractor
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class StepQuizReviewPresenter
@Inject
constructor(
    private val stepQuizReviewInteractor: StepQuizReviewInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StepQuizReviewView>() {
    private var state: StepQuizReviewView.State = StepQuizReviewView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: StepQuizReviewView) {
        super.attachView(view)
        view.setState(state)
    }
}