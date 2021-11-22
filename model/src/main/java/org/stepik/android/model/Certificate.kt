package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readDate
import org.stepik.android.model.util.writeDate
import java.util.Date

data class Certificate(
    @SerializedName("id")
    val id: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("course")
    val course: Long,

    @SerializedName("issue_date")
    val issueDate: Date? = null,
    @SerializedName("update_date")
    val updateDate: Date? = null,

    @SerializedName("grade")
    val grade: String? = null,
    @SerializedName("type")
    val type: Type? = null,
    @SerializedName("url")
    val url: String? = null,

    @SerializedName("saved_fullname")
    val savedFullName: String? = null,
    @SerializedName("edits_count")
    val editsCount: Int,
    @SerializedName("allowed_edits_count")
    val allowedEditsCount: Int
) : Parcelable {

    /*
    Add new in the end, because serialization depends on order.
    */
    enum class Type {
        @SerializedName("regular")
        REGULAR,
        @SerializedName("distinction")
        DISTINCTION
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLong(this.user)
        dest.writeLong(this.course)
        dest.writeDate(issueDate)
        dest.writeDate(updateDate)
        dest.writeString(this.grade)
        dest.writeInt(this.type?.ordinal ?: -1)
        dest.writeString(this.url)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Certificate> {
        override fun createFromParcel(source: Parcel): Certificate =
            Certificate(
                source.readLong(),
                source.readLong(),
                source.readLong(),
                source.readDate(),
                source.readDate(),
                source.readString(),
                Type.values().getOrNull(source.readInt()),
                source.readString(),
                source.readString(),
                source.readInt(),
                source.readInt()
            )

        override fun newArray(size: Int): Array<Certificate?> =
            arrayOfNulls(size)
    }
}