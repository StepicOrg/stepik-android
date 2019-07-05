package org.stepik.android.model.comments

import com.google.gson.annotations.SerializedName

data class Vote(
    @SerializedName("id")
    val id: String,
    @SerializedName("value")
    val value: Value?
) {
    enum class Value {
        @SerializedName("epic")
        LIKE,
        @SerializedName("abuse")
        DISLIKE
    }
}