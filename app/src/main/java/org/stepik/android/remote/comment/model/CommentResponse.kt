package org.stepik.android.remote.comment.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.Vote
import org.stepik.android.model.user.User
import org.stepik.android.remote.base.model.MetaResponse

class CommentResponse(
    @SerializedName("detail")
    val detail: String?, // "You do not have permission to perform this action.", null if OK
    @SerializedName("target")
    val target: List<String>?, // ["Invalid pk '10205111' - object does not exist."], null if OK

    @SerializedName("meta")
    override val meta: Meta, // not null, if OK

    @SerializedName("comments")
    val comments: List<Comment>?,

    @SerializedName("users")
    val users: List<User>?,

    @SerializedName("votes")
    val votes: List<Vote>?,

    @SerializedName("attempts")
    val attempts: List<Attempt>?,

    @SerializedName("submissions")
    val submissions: List<Submission>?
) : MetaResponse