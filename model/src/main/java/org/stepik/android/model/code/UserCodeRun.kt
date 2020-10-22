package org.stepik.android.model.code

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
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
}