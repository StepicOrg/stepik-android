package org.stepic.droid.web

import org.stepic.droid.model.Device
import org.stepik.android.model.Meta

data class DeviceResponse(
        var meta: Meta?,
        var devices: List<Device?>?
)
