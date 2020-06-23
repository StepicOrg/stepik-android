package org.stepik.android.remote.analytic.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.analytic.AnalyticBatchEvent

class AnalyticBatchRequest(
    @SerializedName("batch")
    val batch: List<AnalyticBatchEvent>
)