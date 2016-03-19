package org.stepic.droid.services

import android.content.Intent
import com.google.android.gms.iid.InstanceIDListenerService

class StepicInstanceIDListenerService : InstanceIDListenerService() {
    override fun onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        val intent = Intent(this, RegistrationIntentService::class.java)
        startService(intent)
    }
}