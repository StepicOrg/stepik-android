package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.feedback.Feedback

data class Submission(
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
    @Transient
    val _reply: Reply? = null,
    @SerializedName("attempt")
    val attempt: Long = 0,
    @SerializedName("session")
    val session: String? = null,
    @SerializedName("eta")
    val eta: String? = null,
    @SerializedName("feedback")
    val feedback: Feedback? = null
) : Parcelable {
    @SerializedName("reply")
    private val replyWrapper: ReplyWrapper? = _reply?.let(::ReplyWrapper)

    val reply: Reply?
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(status?.ordinal ?: -1)
        parcel.writeString(score)
        parcel.writeString(hint)
        parcel.writeString(time)
        parcel.writeParcelable(reply, flags)
        parcel.writeLong(attempt)
        parcel.writeString(session)
        parcel.writeString(eta)
        parcel.writeSerializable(feedback)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Submission> {
        override fun createFromParcel(parcel: Parcel): Submission =
            Submission(
                parcel.readLong(),
                Status.values().getOrNull(parcel.readInt()),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),

                parcel.readParcelable(Reply::class.java.classLoader),
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readSerializable() as? Feedback
            )

        override fun newArray(size: Int): Array<Submission?> =
            arrayOfNulls(size)
    }
}