package org.stepik.android.presentation.magic_links

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.magic_links.interactor.MagicLinkInteractor
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class MagicLinkPresenter
@Inject
constructor(
    private val magicLinkInteractor: MagicLinkInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<MagicLinkView>() {
    private var state: MagicLinkView.State = MagicLinkView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    fun onData(url: String) {
        if (state != MagicLinkView.State.Idle) return

        state = MagicLinkView.State.Loading
        compositeDisposable += magicLinkInteractor
            .createMagicLink(url)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = MagicLinkView.State.Success(it.url) },
                onError = { state = MagicLinkView.State.Success(url) }
            )
    }
}