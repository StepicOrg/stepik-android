package org.stepik.android.remote.device.service

import org.stepik.android.remote.device.model.DeviceRequest
import org.stepik.android.remote.device.model.DeviceResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviceService {
    @GET("api/devices")
    fun getDeviceByRegistrationId(@Query("registration_id") token: String): Call<DeviceResponse>

    @POST("api/devices")
    fun registerDevice(@Body deviceRequest: DeviceRequest): Call<DeviceResponse>

    @PUT("api/devices/{id}")
    fun renewDeviceRegistration(@Path("id") deviceId: Long, @Body deviceRequest: DeviceRequest): Call<DeviceResponse>
}