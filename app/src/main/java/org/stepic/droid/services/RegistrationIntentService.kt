package org.stepic.droid.services

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.IApi
import javax.inject.Inject

class RegistrationIntentService : IntentService("StepicGcmReg") {

    @Inject
    lateinit var mSharedPreferences: SharedPreferenceHelper

    @Inject
    lateinit var mApi: IApi

    init {
        MainApplication.component().inject(this)
    }

    override fun onHandleIntent(intent: Intent) {
        try {
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            val instanceID = InstanceID.getInstance(this)
            val token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)

            sendRegistrationToServer(token)

            mSharedPreferences.setIsGcmTokenOk(true)
            YandexMetrica.reportEvent("notification gcm token is updated")
        } catch (e: Exception) {
            YandexMetrica.reportEvent("notification gcm token is not updated")
            mSharedPreferences.setIsGcmTokenOk(false)
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        //        val registrationComplete = Intent(QuickstartPreferences.REGISTRATION_COMPLETE)
        //        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)
    }

    /**
     * Persist registration to third-party servers.

     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.

     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        // Add custom implementation, as needed.
        mApi.registerDevice(token).execute()
    }
}