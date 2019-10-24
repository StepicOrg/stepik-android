package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.readDate
import org.stepik.android.model.util.writeBoolean
import org.stepik.android.model.util.writeDate

import java.util.*

class Lesson(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("cover_url")
    val coverUrl: String? = null,

    @SerializedName("steps")
    val steps: LongArray = longArrayOf(),

    @SerializedName("actions")
    val actions: LessonActions? = null,

    @SerializedName("is_featured")
    val isFeatured: Boolean = false,
    @SerializedName("progress")
    override val progress: String? = null,
    @SerializedName("owner")
    val owner: Long = 0,
    @SerializedName("subscriptions")
    val subscriptions: Array<String>? = null,

    @SerializedName("viewed_by")
    val viewedBy: Long = 0,
    @SerializedName("passed_by")
    val passedBy: Long = 0,

    @SerializedName("vote_delta")
    val voteDelta: Long = 0,

    @SerializedName("language")
    val language: String? = null,
    @SerializedName("is_public")
    val isPublic: Boolean = false,

    @SerializedName("create_date")
    val createDate: Date? = null,
    @SerializedName("update_date")
    val updateDate: Date? = null,

    @SerializedName("learners_group")
    val learnersGroup: String? = null,
    @SerializedName("teachers_group")
    val teachersGroup: String? = null,

    @SerializedName("time_to_complete")
    val timeToComplete: Long = 0
) : Parcelable, Progressable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeString(this.title)
        dest.writeString(this.slug)
        dest.writeString(this.coverUrl)
        dest.writeLongArray(this.steps)
        dest.writeParcelable(this.actions, 0)
        dest.writeBoolean(isFeatured)
        dest.writeString(this.progress)
        dest.writeLong(this.owner)
        dest.writeStringArray(this.subscriptions)
        dest.writeLong(this.viewedBy)
        dest.writeLong(this.passedBy)
        dest.writeLong(this.voteDelta)
        dest.writeString(this.language)
        dest.writeBoolean(isPublic)
        dest.writeDate(this.createDate)
        dest.writeDate(this.updateDate)
        dest.writeString(this.learnersGroup)
        dest.writeString(this.teachersGroup)
    }

    companion object CREATOR : Parcelable.Creator<Lesson> {
        override fun createFromParcel(parcel: Parcel): Lesson =
            Lesson(
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.createLongArray() ?: longArrayOf(),
                parcel.readParcelable(LessonActions::class.java.classLoader),
                parcel.readBoolean(),
                parcel.readString(),
                parcel.readLong(),
                parcel.createStringArray(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readDate(),
                parcel.readDate(),
                parcel.readString(),
                parcel.readString()
            )

        override fun newArray(size: Int): Array<Lesson?> = arrayOfNulls(size)
    }
}
