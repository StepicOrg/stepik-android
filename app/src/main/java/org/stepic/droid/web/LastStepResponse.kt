package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.LastStep
import org.stepik.android.model.Meta

data class LastStepResponse(val meta: Meta?,
                            @SerializedName("last-steps")
                            val lastSteps: List<LastStep>)