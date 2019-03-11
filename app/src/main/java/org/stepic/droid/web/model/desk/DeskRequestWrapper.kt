package org.stepic.droid.web.model.desk

import com.google.gson.annotations.SerializedName

class DeskRequestWrapper(
    @SerializedName("request")
    val request: DeskRequest
) {
    constructor(
        name: String,
        email: String,
        subject: String,
        comment: String,

        link: String,
        technicalDetails: String
    ) : this(
        request = DeskRequest(
            requester = DeskRequester(name, email),
            subject = subject,
            comment = DeskComment(comment),
            customFields = DeskCustomFields(
                link = link,
                technicalDetails = technicalDetails
            )
        )
    )
}