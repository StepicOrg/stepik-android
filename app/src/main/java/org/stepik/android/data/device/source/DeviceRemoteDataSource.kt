package org.stepik.android.data.device.source

import org.stepic.droid.web.DeviceResponse
import retrofit2.Call

interface DeviceRemoteDataSource {
    fun getDevicesByRegistrationId(token: String): Call<DeviceResponse>

    fun renewDeviceRegistration(deviceId: Long, token: String): Call<DeviceResponse>

    fun registerDevice(token: String): Call<DeviceResponse>
}