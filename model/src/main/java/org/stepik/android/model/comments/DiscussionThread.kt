package org.stepik.android.model.comments

import com.google.gson.annotations.SerializedName

data class DiscussionThread(
    @SerializedName("id")
    val id: String,

    @SerializedName("thread")
    val thread: String,

    @SerializedName("discussions_count")
    val discussionsCount: Int,

    @SerializedName("discussion_proxy")
    val discussionProxy: String
) {
    companion object {
        const val THREAD_DEFAULT = "default"
        const val THREAD_SOLUTIONS = "solutions"
    }
}