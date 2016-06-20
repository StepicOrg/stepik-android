package org.stepic.droid.notifications

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.base.MainApplication
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.IApi
import javax.inject.Inject

class StepicInstanceIdService : FirebaseInstanceIdService() {
    val hacker = HackerFcmInstanceId()

    override fun onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        updateAnywhere(hacker.mApi, hacker.mSharedPreferences)
    }

    companion object {
        fun updateAnywhere(mApi: IApi, mSharedPreferences: SharedPreferenceHelper) {
            val tokenNullable : String? = FirebaseInstanceId.getInstance().token
            try {
                val token = tokenNullable!!
                mApi.registerDevice(token).execute()
                mSharedPreferences.setIsGcmTokenOk(true)
                YandexMetrica.reportEvent("notification gcm token is updated")
            } catch (e: Exception) {
                YandexMetrica.reportEvent("notification gcm token is not updated")
                mSharedPreferences.setIsGcmTokenOk(false)
            }
        }
    }

}

class HackerFcmInstanceId() {
    @Inject
    lateinit var mSharedPreferences: SharedPreferenceHelper

    @Inject
    lateinit var mApi: IApi


    init {
        MainApplication.component().inject(this)
    }
}