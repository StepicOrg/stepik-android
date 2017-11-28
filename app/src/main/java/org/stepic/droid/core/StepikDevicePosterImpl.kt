package org.stepic.droid.core

import android.support.annotation.WorkerThread
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
            if (!response.isSuccessful && response.code() != 400) { //400 -- device already registered
                throw Exception("response was failed. it is ok. code: " + response.code())
            }
            sharedPreferencesHelper.setIsGcmTokenOk(true)

            analytic.reportEvent(Analytic.Notification.TOKEN_UPDATED)
        } catch (e: Exception) {
            analytic.reportEvent(Analytic.Notification.TOKEN_UPDATE_FAILED)
            sharedPreferencesHelper.setIsGcmTokenOk(false)
        }
    }
}
