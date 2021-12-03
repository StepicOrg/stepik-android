package org.stepik.android.view.injection.billing

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.jakewharton.rxrelay2.BehaviorRelay
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.nobird.android.view.injection.base.RxScheduler

@Module
class BillingModule {
    /**
     * Provides system checkout that can be used for querying inventory & etc.
     */
    @Provides
    @BillingSingleton
    internal fun provideBillingClient(
        context: Context,
        purchaseListenerBehaviorRelay: BehaviorRelay<Pair<BillingResult, List<Purchase>?>>
    ): BillingClient =
        BillingClient
            .newBuilder(context)
            .setListener { billingResult, mutableList ->
                Log.d("APPS", "Result: ${billingResult.debugMessage} Code: ${billingResult.responseCode}")
                purchaseListenerBehaviorRelay.accept(billingResult to mutableList)
            }
            .enablePendingPurchases()
            .build()
            .also { it.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Log.d("APPS", "onBillingServiceDisconnected")
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    Log.d("APPS", "onBillingSetupFinished - Debug message: ${billingResult.debugMessage}; Response code: ${billingResult.responseCode}")
                }
            })}


    @Provides
    @BillingSingleton
    internal fun providePurchaseListenerRelay(): BehaviorRelay<Pair<BillingResult, List<Purchase>?>> =
        BehaviorRelay.create()


    @Provides
    @RxScheduler.Main
    internal fun provideAndroidScheduler(): Scheduler =
        AndroidSchedulers.mainThread()

    @Provides
    @RxScheduler.Background
    internal fun provideBackgroundScheduler(): Scheduler =
        Schedulers.single()
}