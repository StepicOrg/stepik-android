package org.stepik.android.remote.device

import android.content.Context
import org.stepic.droid.util.DeviceInfoUtil
import org.stepic.droid.web.DeviceRequest
import org.stepic.droid.web.DeviceResponse
import org.stepik.android.data.device.source.DeviceRemoteDataSource
import org.stepik.android.remote.device.service.DeviceService
import retrofit2.Call
import javax.inject.Inject

class DeviceRemoteDataSourceImpl
@Inject
constructor(
    private val deviceService: DeviceService,
    private val context: Context // TODO This probably should be here?
) : DeviceRemoteDataSource {
    override fun getDevicesByRegistrationId(token: String): Call<DeviceResponse> =
        deviceService.getDeviceByRegistrationId(token)

    override fun renewDeviceRegistration(deviceId: Long, token: String): Call<DeviceResponse> {
        val description = DeviceInfoUtil.getShortInfo(context)
        val deviceRequest = DeviceRequest(deviceId, token, description)
        return deviceService.renewDeviceRegistration(deviceId, deviceRequest)
    }

    override fun registerDevice(token: String): Call<DeviceResponse> {
        val description = DeviceInfoUtil.getShortInfo(context)
        val deviceRequest = DeviceRequest(token = token, description = description)
        return deviceService.registerDevice(deviceRequest)
    }
}