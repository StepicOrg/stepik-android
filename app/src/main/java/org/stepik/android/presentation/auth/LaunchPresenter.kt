package org.stepik.android.presentation.auth

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class LaunchPresenter
@Inject
constructor(

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<LaunchView>() {
    private var state: LaunchView.State = LaunchView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: LaunchView) {
        super.attachView(view)
        view.setState(state)
    }
}