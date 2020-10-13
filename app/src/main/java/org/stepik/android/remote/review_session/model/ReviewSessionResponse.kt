package org.stepik.android.remote.review_session.model

import org.stepik.android.domain.review_session.model.ReviewSession
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.remote.base.model.MetaResponse

class ReviewSessionResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("review-sessions")
    val reviewSessions: List<ReviewSession>,

    @SerializedName("attempts")
    val attempts: List<Attempt>,
    @SerializedName("submissions")
    val submissions: List<Submission>

//    @SerializedName("reviews")
//    @SerializedName("rubric-scores")
) : MetaResponse