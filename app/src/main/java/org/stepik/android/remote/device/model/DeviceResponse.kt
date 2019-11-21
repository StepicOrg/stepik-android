package org.stepik.android.remote.device.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.Device
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class DeviceResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("devices")
    val devices: List<Device>
) : MetaResponse
