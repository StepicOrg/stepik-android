package org.stepik.android.view.injection.billing

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import ru.nobird.android.view.injection.base.RxScheduler

@Module
class BillingModule {
    @Provides
    internal fun provideBilling(
        context: Context,
        @PublicLicenseKey
        appPublicLicenseKey: String
    ): Billing =
        Billing(context, object : Billing.DefaultConfiguration() {
            override fun getPublicKey(): String =
                appPublicLicenseKey
        })

    /**
     * Provides system checkout that can be used for querying inventory & etc.
     */
    @Provides
    @SystemCheckout
    internal fun provideSystemCheckout(billing: Billing): Checkout =
        Checkout
            .forApplication(billing)
            .also(Checkout::start)

    @Provides
    @RxScheduler.Main
    internal fun provideAndroidScheduler(): Scheduler =
        AndroidSchedulers.mainThread()

    @Provides
    @RxScheduler.Background
    internal fun provideBackgroundScheduler(): Scheduler =
        Schedulers.io()
}