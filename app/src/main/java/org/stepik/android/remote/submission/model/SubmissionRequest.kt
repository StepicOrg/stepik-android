package org.stepik.android.remote.submission.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission

class SubmissionRequest(
    @SerializedName("submission")
    val submission: Submission
) {
    constructor(reply: Reply, attemptId: Long) : this(Submission(_reply = reply, attempt = attemptId))
}