package org.stepik.android.presentation.course_purchase.dispatcher

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_purchase.interactor.CoursePurchaseInteractor
import org.stepik.android.domain.wishlist.analytic.CourseWishlistAddedEvent
import org.stepik.android.domain.wishlist.interactor.WishlistInteractor
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.view.injection.billing.BillingSingleton
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import timber.log.Timber
import javax.inject.Inject

class CoursePurchaseActionDispatcher
@Inject
constructor(
    @BillingSingleton
    purchaseListenerBehaviorRelay: BehaviorRelay<Pair<BillingResult, List<Purchase>?>>,

    private val analytic: Analytic,
    private val wishlistInteractor: WishlistInteractor,
    private val coursePurchaseInteractor: CoursePurchaseInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CoursePurchaseFeature.Action, CoursePurchaseFeature.Message>() {
    init {
        compositeDisposable += purchaseListenerBehaviorRelay
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = { (billingResult, purchases) ->
                    Timber.d("Action dispatcher; Billing Result - Debug message: ${billingResult.debugMessage} Response: ${billingResult.responseCode}; Purchases - $purchases")
                }
            )
    }
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
                            if (it == PromoCodeSku.EMPTY) {
                                onNewMessage(CoursePurchaseFeature.Message.PromoCodeInvalidMessage)
                            } else {
                                onNewMessage(CoursePurchaseFeature.Message.PromoCodeValidMessage(it))
                            }
                        },
                        onError = { onNewMessage(CoursePurchaseFeature.Message.PromoCodeInvalidMessage) }
                    )
            }
            is CoursePurchaseFeature.Action.FetchSkuDetails -> {
                compositeDisposable += coursePurchaseInteractor
                    .getSkuDetails(action.skuId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CoursePurchaseFeature.Message.BuyCourseSkuDetailsSuccess(it)) },
                        onError = { onNewMessage(CoursePurchaseFeature.Message.BuyCourseSkuDetailsFailure(it)) }
                    )
            }
        }
    }
}