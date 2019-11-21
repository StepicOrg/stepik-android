package org.stepic.droid.core

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.firebase.iid.FirebaseInstanceId
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DeviceInfoUtil
import org.stepik.android.domain.device.repository.DeviceRepository
import org.stepik.android.remote.device.model.DeviceRequest
import retrofit2.HttpException
import javax.inject.Inject

@AppSingleton
class StepikDevicePosterImpl
@Inject
constructor(
    private val context: Context,
    private val firebaseInstanceId: FirebaseInstanceId,
    private val deviceRepository: DeviceRepository,
    private val sharedPreferencesHelper: SharedPreferenceHelper,
    private val analytic: Analytic
) : StepikDevicePoster {

    @WorkerThread
    override fun registerDevice() {
        val tokenNullable: String? = firebaseInstanceId.token
        try {
            sharedPreferencesHelper.authResponseFromStore!! //for logged user only work
            val token = tokenNullable!!
            deviceRepository.registerDevice(createDeviceRequest(token)).blockingAwait()
            sharedPreferencesHelper.setIsGcmTokenOk(true)
            analytic.reportEvent(Analytic.Notification.TOKEN_UPDATED)
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 400) {
                renewDeviceRegistration(tokenNullable!!)
            }
            analytic.reportEvent(Analytic.Notification.TOKEN_UPDATE_FAILED)
            sharedPreferencesHelper.setIsGcmTokenOk(false)
        }
    }

    private fun renewDeviceRegistration(token: String) {
        val deviceId = deviceRepository.getDevicesByRegistrationId(token).blockingGet().firstOrNull()?.id
        if (deviceId != null) {
            deviceRepository.renewDeviceRegistration(deviceId, createDeviceRequest(token)).blockingAwait()
        } else {
            throw Exception("Can't get device id for token: $token")
        }
    }

    private fun createDeviceRequest(token: String): DeviceRequest {
        val description = DeviceInfoUtil.getShortInfo(context)
        return DeviceRequest(token = token, description = description)
    }
}
