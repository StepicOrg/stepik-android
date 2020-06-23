package org.stepik.android.model.analytic

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class AnalyticBatchEvent(
    @SerializedName("name")
    val name: String,
    @SerializedName("timestamp")
    val timeStamp: Long,
    @SerializedName("tags")
    val tags: JsonElement?,
    @SerializedName("data")
    val data: JsonElement?
)