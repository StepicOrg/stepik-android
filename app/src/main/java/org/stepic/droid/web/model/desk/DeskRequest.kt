package org.stepic.droid.web.model.desk

import com.google.gson.annotations.SerializedName

class DeskRequest(
    @SerializedName("requester")
    val requester: DeskRequester,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("comment")
    val comment: DeskComment,
    @SerializedName("custom_fields")
    val customFields: DeskCustomFields
)