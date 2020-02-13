package org.stepik.android.model.code

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.readDate
import org.stepik.android.model.util.writeBoolean
import org.stepik.android.model.util.writeDate
import java.util.Date

data class UserCodeRun(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("user")
    val user: Long = 0,
    @SerializedName("step")
    val step: Long = 0,
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("status")
    val status: Status? = null,
    @SerializedName("stdin")
    val stdin: String? = null,
    @SerializedName("stdout")
    val stdout: String? = null,
    @SerializedName("stderr")
    val stderr: String? = null,
    @SerializedName("time_limit_exceeded")
    val timeLimitExceeded: Boolean = false,
    @SerializedName("memory_limit_exceeded")
    val memoryLimitExceeded: Boolean = false,
    @SerializedName("create_date")
    val createDate: Date? = null,
    @SerializedName("update_date")
    val updateDate: Date? = null
) : Parcelable {

    enum class Status(val scope: String) {
        @SerializedName("success")
        SUCCESS("success"),

        @SerializedName("failure")
        FAILURE("failure"),

        @SerializedName("evaluation")
        EVALUATION("evaluation")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(user)
        parcel.writeLong(step)
        parcel.writeString(language)
        parcel.writeString(code)
        parcel.writeInt(status?.ordinal ?: -1)
        parcel.writeString(stdin)
        parcel.writeString(stdout)
        parcel.writeString(stderr)
        parcel.writeBoolean(timeLimitExceeded)
        parcel.writeBoolean(memoryLimitExceeded)
        parcel.writeDate(createDate)
        parcel.writeDate(updateDate)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<UserCodeRun> {
        override fun createFromParcel(parcel: Parcel): UserCodeRun =
            UserCodeRun(
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                Status.values().getOrNull(parcel.readInt()),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readDate(),
                parcel.readDate()
            )

        override fun newArray(size: Int): Array<UserCodeRun?> =
            arrayOfNulls(size)
    }
}