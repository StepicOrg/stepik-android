package org.stepik.android.remote.analytic.model

import com.google.gson.annotations.SerializedName

class AnalyticBatchRequest(
    @SerializedName("batch")
    val batch: List<AnalyticBatchEvent>
)