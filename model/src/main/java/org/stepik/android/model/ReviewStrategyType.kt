package org.stepik.android.model

import com.google.gson.annotations.SerializedName

enum class ReviewStrategyType {
    @SerializedName("peer")
    PEER,
    @SerializedName("instructor")
    INSTRUCTOR
}