package org.stepic.droid.core

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class GoogleApiCheckerImpl
@Inject
constructor(
        private val context: Context,
        private val analytic: Analytic
) : GoogleApiChecker {

    override fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                //do not show Google Services dialog
                analytic.reportEvent(Analytic.Error.GOOGLE_SERVICES_TOO_OLD) //it is resolvable, but we do not want push user for updating services
            }
            false
        } else {
            true
        }
    }
}
