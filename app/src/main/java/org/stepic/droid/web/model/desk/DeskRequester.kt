package org.stepic.droid.web.model.desk

import com.google.gson.annotations.SerializedName

class DeskRequester(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
)