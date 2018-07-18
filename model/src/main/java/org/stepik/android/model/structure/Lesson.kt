package org.stepik.android.model.structure

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Progressable
import org.stepik.android.model.readBoolean
import org.stepik.android.model.writeBoolean

import java.io.Serializable
import java.util.*

class Lesson(
        val id: Long = 0,
        val steps: LongArray = longArrayOf(),
        val tags: IntArray? = null,
        val playlists: Array<String>? = null,

        @SerializedName("is_featured")
        val isFeatured: Boolean = false,
        @SerializedName("is_prime")
        val isPrime: Boolean = false,
        override val progress: String? = null,
        val owner: Int = 0,
        val subscriptions: Array<String>? = null,

        @SerializedName("viewed_by")
        val viewedBy: Int = 0,
        @SerializedName("passed_by")
        val passedBy: Int = 0,

        val dependencies: Array<String>? = null,
        val followers: Array<String>? = null,
        val language: String? = null,
        @SerializedName("is_public")
        val isPublic: Boolean = false,
        val title: String? = null,
        val slug: String? = null,

        @SerializedName("create_date")
        val createDate: Date? = null,
        @SerializedName("update_date")
        val updateDate: Date? = null,

        @SerializedName("learners_group")
        val learnersGroup: String? = null,
        @SerializedName("teacher_group")
        val teacherGroup: String? = null,
        @SerializedName("cover_url")
        val coverUrl: String? = null,

        // todo: remove this after downloads refactoring
        var isCached: Boolean = false,
        var isLoading: Boolean = false,

        @SerializedName("time_to_complete")
        val timeToComplete: Long = 0

) : Parcelable, Serializable, Progressable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLongArray(this.steps)
        dest.writeIntArray(this.tags)
        dest.writeStringArray(this.playlists)
        dest.writeBoolean(isFeatured)
        dest.writeBoolean(isPrime)
        dest.writeString(this.progress)
        dest.writeInt(this.owner)
        dest.writeStringArray(this.subscriptions)
        dest.writeInt(this.viewedBy)
        dest.writeInt(this.passedBy)
        dest.writeStringArray(this.dependencies)
        dest.writeStringArray(this.followers)
        dest.writeString(this.language)
        dest.writeBoolean(isPublic)
        dest.writeString(this.title)
        dest.writeString(this.slug)
        dest.writeSerializable(this.createDate)
        dest.writeSerializable(this.updateDate)
        dest.writeString(this.learnersGroup)
        dest.writeString(this.teacherGroup)
        dest.writeString(this.coverUrl)
        dest.writeBoolean(isCached)
        dest.writeBoolean(isLoading)
    }

    companion object CREATOR : Parcelable.Creator<Lesson> {
        override fun createFromParcel(parcel: Parcel): Lesson = Lesson(
                parcel.readLong(),
                parcel.createLongArray(),
                parcel.createIntArray(),
                parcel.createStringArray(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readString(),
                parcel.readInt(),
                parcel.createStringArray(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.createStringArray(),
                parcel.createStringArray(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readString(),
                parcel.readString(),
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readBoolean()
        )

        override fun newArray(size: Int): Array<Lesson?> = arrayOfNulls(size)
    }
}
