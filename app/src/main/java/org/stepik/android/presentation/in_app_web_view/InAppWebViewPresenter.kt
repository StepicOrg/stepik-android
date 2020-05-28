package org.stepik.android.presentation.in_app_web_view

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.magic_links.interactor.MagicLinkInteractor
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class InAppWebViewPresenter
@Inject
constructor(
    private val magicLinkInteractor: MagicLinkInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<InAppWebViewView>() {
    private var state: InAppWebViewView.State = InAppWebViewView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: InAppWebViewView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onData(url: String, isProvideAuth: Boolean, forceUpdate: Boolean = false) {
        if (state != InAppWebViewView.State.Idle &&
            !(state is InAppWebViewView.State.Error && forceUpdate)) {
            return
        }

        if (isProvideAuth) {
            compositeDisposable += magicLinkInteractor
                .createMagicLink(url)
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribeBy(
                    onSuccess = { state = InAppWebViewView.State.WebLoading(it.url) },
                    onError = { state = InAppWebViewView.State.Error }
                )
        } else {
            state = InAppWebViewView.State.WebLoading(url)
        }
    }

    fun onSuccess() {
        if (state !is InAppWebViewView.State.WebLoading) {
            return
        }
        state = InAppWebViewView.State.Success
    }

    fun onError() {
        if (state !is InAppWebViewView.State.WebLoading) {
            return
        }
        state = InAppWebViewView.State.Error
    }
}