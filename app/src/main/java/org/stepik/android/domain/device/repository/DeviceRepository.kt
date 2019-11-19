package org.stepik.android.domain.device.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.Device
import org.stepik.android.remote.device.model.DeviceRequest

interface DeviceRepository {
    fun getDevicesByRegistrationId(token: String): Single<List<Device?>?>
    fun renewDeviceRegistration(deviceId: Long, deviceRequest: DeviceRequest): Completable
    fun registerDevice(deviceRequest: DeviceRequest): Completable
}