package org.stepik.android.model.comments

import com.google.gson.annotations.SerializedName

data class Vote(
        val id: String,
        val value: Value?
) {
    enum class Value {
        @SerializedName("epic")
        LIKE,
        @SerializedName("abuse")
        DISLIKE,
    }
}