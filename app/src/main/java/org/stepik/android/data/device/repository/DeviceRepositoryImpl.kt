package org.stepik.android.data.device.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.Device
import org.stepik.android.data.device.source.DeviceRemoteDataSource
import org.stepik.android.domain.device.repository.DeviceRepository
import org.stepik.android.remote.device.model.DeviceRequest
import javax.inject.Inject

class DeviceRepositoryImpl
@Inject
constructor(
    private val deviceRemoteDataSource: DeviceRemoteDataSource
) : DeviceRepository {
    override fun getDevicesByRegistrationId(token: String): Single<List<Device?>?> =
        deviceRemoteDataSource.getDevicesByRegistrationId(token)

    override fun renewDeviceRegistration(deviceId: Long, deviceRequest: DeviceRequest): Completable =
        deviceRemoteDataSource.renewDeviceRegistration(deviceId, deviceRequest)

    override fun registerDevice(deviceRequest: DeviceRequest): Completable =
        deviceRemoteDataSource.registerDevice(deviceRequest)
}