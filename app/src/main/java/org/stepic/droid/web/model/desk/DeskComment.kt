package org.stepic.droid.web.model.desk

import com.google.gson.annotations.SerializedName

class DeskComment(
    @SerializedName("html_body")
    val htmlBody: String
)