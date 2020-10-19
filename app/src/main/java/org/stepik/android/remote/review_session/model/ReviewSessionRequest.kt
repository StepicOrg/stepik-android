package org.stepik.android.remote.review_session.model

import com.google.gson.annotations.SerializedName

class ReviewSessionRequest(
    @SerializedName("reviewSession")
    val body: Body
) {
    constructor(submission: Long) : this(Body(submission))

    class Body(
        @SerializedName("submission")
        val submission: Long
    )
}