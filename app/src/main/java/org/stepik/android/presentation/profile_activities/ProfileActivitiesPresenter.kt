package org.stepik.android.presentation.profile_activities

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileActivitiesPresenter
@Inject
constructor(

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileActivitiesView>() {
    private var state: ProfileActivitiesView.State = ProfileActivitiesView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: ProfileActivitiesView) {
        super.attachView(view)
        view.setState(state)
    }
}