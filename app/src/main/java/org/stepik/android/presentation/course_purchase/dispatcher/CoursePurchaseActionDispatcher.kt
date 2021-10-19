package org.stepik.android.presentation.course_purchase.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_purchase.interactor.CoursePurchaseInteractor
import org.stepik.android.domain.wishlist.analytic.CourseWishlistAddedEvent
import org.stepik.android.domain.wishlist.interactor.WishlistInteractor
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CoursePurchaseActionDispatcher
@Inject
constructor(
    private val analytic: Analytic,
    private val wishlistInteractor: WishlistInteractor,
    private val coursePurchaseInteractor: CoursePurchaseInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CoursePurchaseFeature.Action, CoursePurchaseFeature.Message>() {
    override fun handleAction(action: CoursePurchaseFeature.Action) {
        when (action) {
            is CoursePurchaseFeature.Action.AddToWishlist -> {
                compositeDisposable += wishlistInteractor
                    .updateWishlistWithOperation(action.wishlistEntity, action.wishlistOperationData)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = {
                            analytic.report(CourseWishlistAddedEvent(action.course, CourseViewSource.CoursePurchase))
                            onNewMessage(CoursePurchaseFeature.Message.WishlistAddSuccess(it))
                        },
                        onError = { onNewMessage(CoursePurchaseFeature.Message.WishlistAddFailure) }
                    )
            }
            is CoursePurchaseFeature.Action.CheckPromoCode -> {
                compositeDisposable += coursePurchaseInteractor
                    .checkPromoCodeValidity(action.courseId, action.promoCodeName)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = {
                            if (it == DeeplinkPromoCode.EMPTY) {
                                onNewMessage(CoursePurchaseFeature.Message.PromoCodeInvalidMessage)
                            } else {
                                onNewMessage(CoursePurchaseFeature.Message.PromoCodeValidMessage(it))
                            }
                        },
                        onError = { onNewMessage(CoursePurchaseFeature.Message.PromoCodeInvalidMessage) }
                    )
            }
        }
    }
}