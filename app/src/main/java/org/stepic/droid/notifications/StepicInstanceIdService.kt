package org.stepic.droid.notifications

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.MainApplication
import org.stepic.droid.model.Profile
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.IApi
import javax.inject.Inject

class StepicInstanceIdService : FirebaseInstanceIdService() {
    val hacker = HackerFcmInstanceId()

    override fun onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        updateAnywhere(hacker.mApi, hacker.mSharedPreferences, hacker.analytic)
    }

    companion object {
        fun updateAnywhere(mApi: IApi, mSharedPreferences: SharedPreferenceHelper, analytic: Analytic) {
            val tokenNullable : String? = FirebaseInstanceId.getInstance().token
            try {
                val profile : Profile = mSharedPreferences.profile!!
                val token = tokenNullable!!
                val response = mApi.registerDevice(token).execute()
                if (!response.isSuccess && response.code() != 400) { //400 -- device already registered
                    throw Exception("response was failed. it is ok. code: " + response.code())
                }
                mSharedPreferences.setIsGcmTokenOk(true)

                analytic.reportEvent(Analytic.Notification.TOKEN_UPDATED)
            } catch (e: Exception) {
                analytic.reportEvent(Analytic.Notification.TOKEN_UPDATE_FAILED)
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

    @Inject
    lateinit var analytic : Analytic


    init {
        MainApplication.component().inject(this)
    }
}