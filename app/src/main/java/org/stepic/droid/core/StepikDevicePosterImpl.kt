package org.stepic.droid.core

import androidx.annotation.WorkerThread
import com.google.firebase.iid.FirebaseInstanceId
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api
import javax.inject.Inject

@AppSingleton
class StepikDevicePosterImpl
@Inject
constructor(
        private val firebaseInstanceId: FirebaseInstanceId,
        private val api: Api,
        private val sharedPreferencesHelper: SharedPreferenceHelper,
        private val analytic: Analytic
) : StepikDevicePoster {

    @WorkerThread
    override fun registerDevice() {
        val tokenNullable: String? = firebaseInstanceId.token
        try {
            sharedPreferencesHelper.authResponseFromStore!! //for logged user only work
            val token = tokenNullable!!
            val response = api.registerDevice(token).execute()
            if (!response.isSuccessful) { //400 -- device already registered
                if (response.code() == 400) {
                    renewDeviceRegistration(token)
                } else {
                    throw Exception("response was failed. it is ok. code: " + response.code())
                }
            }
            sharedPreferencesHelper.setIsGcmTokenOk(true)

            analytic.reportEvent(Analytic.Notification.TOKEN_UPDATED)
        } catch (e: Exception) {
            analytic.reportEvent(Analytic.Notification.TOKEN_UPDATE_FAILED)
            sharedPreferencesHelper.setIsGcmTokenOk(false)
        }
    }

    private fun renewDeviceRegistration(token: String) {
        val deviceId = api.getDevicesByRegistrationId(token).execute()?.body()?.devices?.firstOrNull()?.id
        if (deviceId != null) {
            val response = api.renewDeviceRegistration(deviceId, token).execute()
            if (!response.isSuccessful) {
                throw Exception("Can't renew device registration for device: $deviceId")
            }
        } else {
            throw Exception("Can't get device id for token: $token")
        }
    }
}
