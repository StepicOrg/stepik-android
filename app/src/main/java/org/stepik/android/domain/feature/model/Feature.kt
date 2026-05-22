package org.stepik.android.domain.feature.model

import com.google.gson.annotations.SerializedName

data class Feature(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("parameters")
    val parameters: Parameters
)