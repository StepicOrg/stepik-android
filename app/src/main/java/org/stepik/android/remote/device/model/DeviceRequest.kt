package org.stepik.android.remote.device.model

import org.stepic.droid.model.ClientType
import org.stepic.droid.model.Device

class DeviceRequest
@JvmOverloads
constructor(deviceId: Long = 0, token: String, description: String) {
    private val device = Device(deviceId, token, 0, description, ClientType.Android, isBadgesEnabled = true)
}
