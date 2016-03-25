package org.stepic.droid.web

import org.stepic.droid.model.Device
import org.stepic.droid.model.Meta

data class DeviceResponse(
        var meta: Meta?,
        var devices: List<Device?>?
)
