package org.stepik.android.presentation.banner.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.banner.interactor.BannerInteractor
import org.stepik.android.presentation.banner.BannerFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class BannerActionDispatcher
@Inject
constructor(
    private val bannerInteractor: BannerInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<BannerFeature.Action, BannerFeature.Message>() {
    override fun handleAction(action: BannerFeature.Action) {
        when (action) {
            is BannerFeature.Action.LoadBanners -> {
                compositeDisposable += bannerInteractor
                    .getBanners(action.screen)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(BannerFeature.Message.BannersResult(it)) },
                        onError = { onNewMessage(BannerFeature.Message.BannersError) }
                    )
            }
        }
    }
}