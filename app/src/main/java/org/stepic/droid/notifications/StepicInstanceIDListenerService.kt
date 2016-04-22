package org.stepic.droid.notifications

import android.content.Intent
import com.google.android.gms.iid.InstanceIDListenerService
import org.stepic.droid.notifications.RegistrationIntentService

class StepicInstanceIDListenerService : InstanceIDListenerService() {
    override fun onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        val intent = Intent(this, RegistrationIntentService::class.java)
        startService(intent)
    }
}