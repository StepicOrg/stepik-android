package org.stepik.android.presentation.submission

import org.stepik.android.domain.submission.interactor.SubmissionInteractor
import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class SubmissionsPresenter
@Inject
constructor(
    private val submissionInteractor: SubmissionInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<SubmissionsView>() {
    private var state: SubmissionsView.State = SubmissionsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: SubmissionsView) {
        super.attachView(view)
        view.setState(state)
    }
}