package org.stepik.android.remote.comment.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.comments.Comment

class CommentRequest(
    @SerializedName("comment")
    val comment: Comment
)