package org.stepik.android.presentation.in_app_web_view

import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class InAppWebViewPresenter
@Inject
constructor() : PresenterBase<InAppWebViewView>() {
    private var state: InAppWebViewView.State = InAppWebViewView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: InAppWebViewView) {
        super.attachView(view)
        view.setState(state)
    }

    fun startLoading(forceUpdate: Boolean = false) {
        if (state != InAppWebViewView.State.Idle && !(state == InAppWebViewView.State.Error && forceUpdate)) {
            return
        }
        state = InAppWebViewView.State.Loading
    }

    fun onSuccess() {
        if (state != InAppWebViewView.State.Loading) {
            return
        }
        state = InAppWebViewView.State.Success
    }

    fun onError() {
        if (state != InAppWebViewView.State.Loading) {
            return
        }
        state = InAppWebViewView.State.Error
    }
}