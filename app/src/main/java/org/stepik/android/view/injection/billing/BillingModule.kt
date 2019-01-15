package org.stepik.android.view.injection.billing

import android.content.Context
import dagger.Module
import dagger.Provides
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton

@Module
class BillingModule {
    @Provides
    @AppSingleton
    internal fun provideBilling(context: Context, config: Config): Billing =
        Billing(context, object : Billing.DefaultConfiguration() {
            override fun getPublicKey(): String =
                config.appPublicLicenseKey
        })

    /**
     * Provides system checkout that can be used for querying inventory & etc.
     */
    @Provides
    @AppSingleton
    @SystemCheckout
    internal fun provideSystemCheckout(billing: Billing): Checkout =
        Checkout
            .forApplication(billing)
            .also(Checkout::start)
}