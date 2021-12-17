package org.stepik.android.presentation.wishlist.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.wishlist.interactor.WishlistInteractor
import org.stepik.android.presentation.wishlist.WishlistOperationFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class WishlistOperationActionDispatcher
@Inject
constructor(
    private val analytic: Analytic,
    private val wishlistInteractor: WishlistInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<WishlistOperationFeature.Action, WishlistOperationFeature.Message>() {
    override fun handleAction(action: WishlistOperationFeature.Action) {
        when (action) {
            is WishlistOperationFeature.Action.AddToWishlist -> {
                compositeDisposable += wishlistInteractor
                    .updateWishlistWithOperation(action.wishlistOperationData)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = { onNewMessage(WishlistOperationFeature.Message.WishlistAddSuccess(action.course, action.courseViewSource)) },
                        onError = { onNewMessage(WishlistOperationFeature.Message.WishlistAddFailure) }
                    )
            }
            is WishlistOperationFeature.Action.LogAnalyticEvent ->
                analytic.report(action.analyticEvent)
        }
    }
}