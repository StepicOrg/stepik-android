package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.RemoteLastStep
import org.stepik.android.model.Meta

class LastStepResponse(
    val meta: Meta?,
    @SerializedName("last-steps")
    val lastSteps: List<RemoteLastStep>
)