package org.stepik.android.presentation.wishlist.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.wishlist.WishlistInteractor
import org.stepik.android.presentation.wishlist.WishlistFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class WishlistActionDispatcher
@Inject
constructor(
    private val wishlistInteractor: WishlistInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<WishlistFeature.Action, WishlistFeature.Message>() {
    override fun handleAction(action: WishlistFeature.Action) {
        when (action) {
            is WishlistFeature.Action.FetchWishList -> {
                compositeDisposable += wishlistInteractor
                    .getWishlist()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(WishlistFeature.Message.FetchWishlistSuccess(it)) },
                        onError = { onNewMessage(WishlistFeature.Message.FetchWishListError) }
                    )
            }
        }
    }
}