package org.stepik.android.remote.device

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.Device
import org.stepik.android.data.device.source.DeviceRemoteDataSource
import org.stepik.android.remote.device.model.DeviceRequest
import org.stepik.android.remote.device.model.DeviceResponse
import org.stepik.android.remote.device.service.DeviceService
import javax.inject.Inject

class DeviceRemoteDataSourceImpl
@Inject
constructor(
    private val deviceService: DeviceService
) : DeviceRemoteDataSource {
    override fun getDevicesByRegistrationId(token: String): Single<List<Device>> =
        deviceService
            .getDeviceByRegistrationId(token)
            .map(DeviceResponse::devices)

    override fun renewDeviceRegistration(deviceId: Long, deviceRequest: DeviceRequest): Completable =
        deviceService
            .renewDeviceRegistration(deviceId, deviceRequest)

    override fun registerDevice(deviceRequest: DeviceRequest): Completable =
        deviceService
            .registerDevice(deviceRequest)
}