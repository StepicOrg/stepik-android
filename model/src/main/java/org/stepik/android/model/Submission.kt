package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class Submission(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("status")
    val status: Status? = null,
    @SerializedName("score")
    val score: String? = null,
    @SerializedName("hint")
    val hint: String? = null,
    @SerializedName("time")
    val time: String? = null,
    reply: Reply? = null,
    @SerializedName("attempt")
    val attempt: Long = 0,
    @SerializedName("session")
    val session: String? = null,
    @SerializedName("eta")
    val eta: String? = null
) {
    @Deprecated("this compatibility constructor will be removed after rewriting StepAttemptFragment in Kotlin")
    constructor(reply: Reply?, attempt: Long, status: Status?): this(id = 0, reply = reply, attempt = attempt, status = status)

    @SerializedName("reply")
    private val replyWrapper: ReplyWrapper? = reply?.let(::ReplyWrapper)

    val reply: Reply? // this virtual property allows to work with reply like it regular class field without additional wrapper
        get() = replyWrapper?.reply

    enum class Status(val scope: String) {
        @SerializedName("correct")
        CORRECT("correct"),

        @SerializedName("wrong")
        WRONG("wrong"),

        @SerializedName("evaluation")
        EVALUATION("evaluation"),

        @SerializedName("local")
        LOCAL("local")
    }
}