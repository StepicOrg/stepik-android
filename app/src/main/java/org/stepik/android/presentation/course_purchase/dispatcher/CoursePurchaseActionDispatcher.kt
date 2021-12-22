package org.stepik.android.presentation.course_purchase.dispatcher

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_purchase.error.BillingException
import org.stepik.android.domain.course_purchase.interactor.CoursePurchaseInteractor
import org.stepik.android.domain.feedback.interactor.FeedbackInteractor
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.view.injection.billing.BillingSingleton
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CoursePurchaseActionDispatcher
@Inject
constructor(
    @BillingSingleton
    purchaseListenerBehaviorRelay: PublishRelay<Pair<BillingResult, List<Purchase>?>>,

    private val analytic: Analytic,
    private val feedbackInteractor: FeedbackInteractor,
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
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                        onNewMessage(CoursePurchaseFeature.Message.PurchaseFlowBillingSuccess(purchases))
                    } else {
                        onNewMessage(CoursePurchaseFeature.Message.PurchaseFlowBillingFailure(BillingException(billingResult.responseCode, billingResult.debugMessage)))
                    }
                }
            )
    }
    override fun handleAction(action: CoursePurchaseFeature.Action) {
        when (action) {
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
            is CoursePurchaseFeature.Action.FetchLaunchFlowData -> {
                compositeDisposable += coursePurchaseInteractor
                    .fetchPurchaseFlowData(action.courseId, action.skuId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { (obfuscatedParams, skuDetails) -> onNewMessage(CoursePurchaseFeature.Message.LaunchPurchaseFlowSuccess(obfuscatedParams, skuDetails)) },
                        onError = { onNewMessage(CoursePurchaseFeature.Message.LaunchPurchaseFlowFailure(it)) }
                    )
            }
            is CoursePurchaseFeature.Action.ConsumePurchaseAction -> {
                compositeDisposable += coursePurchaseInteractor
                    .consumePurchase(action.courseId, action.skuDetails, action.purchase, action.promoCode)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = { onNewMessage(CoursePurchaseFeature.Message.ConsumePurchaseSuccess) },
                        onError = { onNewMessage(CoursePurchaseFeature.Message.ConsumePurchaseFailure(it)) }
                    )
            }
            is CoursePurchaseFeature.Action.RestorePurchase -> {
                compositeDisposable += coursePurchaseInteractor
                    .restorePurchase(action.courseId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = { onNewMessage(CoursePurchaseFeature.Message.RestorePurchaseSuccess) },
                        onError = { onNewMessage(CoursePurchaseFeature.Message.RestorePurchaseFailure(it)) }
                    )
            }
            is CoursePurchaseFeature.Action.GenerateSupportEmailData -> {
                compositeDisposable += feedbackInteractor
                    .createSupportEmailData(action.subject, action.deviceInfo)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(CoursePurchaseFeature.Message.SetupFeedbackSuccess(it)) },
                        onError = emptyOnErrorStub
                    )
            }
            is CoursePurchaseFeature.Action.LogAnalyticEvent ->
                analytic.report(action.analyticEvent)
        }
    }
}