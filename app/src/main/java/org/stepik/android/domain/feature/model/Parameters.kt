package org.stepik.android.domain.feature.model

import com.google.gson.annotations.SerializedName

data class Parameters(
    @SerializedName("cacheFile")
    val cacheFile: String? = null,
    @SerializedName("isEnabled")
    val isEnabled: Boolean? = null
)
