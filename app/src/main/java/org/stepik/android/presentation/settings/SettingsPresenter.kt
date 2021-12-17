package org.stepik.android.presentation.settings

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.StepikLogoutManager
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.feedback.interactor.FeedbackInteractor
import org.stepik.android.presentation.base.PresenterBase
import ru.nobird.android.domain.rx.emptyOnErrorStub
import javax.inject.Inject

class SettingsPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val stepikLogoutManager: StepikLogoutManager,

    private val feedbackInteractor: FeedbackInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
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

    fun contactSupport(subject: String, aboutSystemInfo: String) {
        compositeDisposable += feedbackInteractor
            .createSupportEmailData(subject, aboutSystemInfo)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { view?.sendTextFeedback(it) },
                onError = emptyOnErrorStub
            )
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