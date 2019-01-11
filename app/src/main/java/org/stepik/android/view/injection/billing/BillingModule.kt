package org.stepik.android.view.injection.billing

import android.content.Context
import dagger.Module
import dagger.Provides
import org.solovyev.android.checkout.Billing
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
}