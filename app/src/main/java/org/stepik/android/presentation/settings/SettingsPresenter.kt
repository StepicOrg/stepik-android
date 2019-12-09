package org.stepik.android.presentation.settings

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.StepikLogoutManager
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class SettingsPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val stepikLogoutManager: StepikLogoutManager
) : PresenterBase<SettingsView>() {
    private var isBlockingLoading: Boolean = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    override fun attachView(view: SettingsView) {
        super.attachView(view)
        view.setBlockingLoading(isBlockingLoading)
    }

    fun onLogoutClicked() {
        isBlockingLoading = true
        analytic.reportEvent(Analytic.Interaction.CLICK_YES_LOGOUT)
        stepikLogoutManager.logout {
            isBlockingLoading = false
            view?.onLogoutSuccess()
        }
    }
}