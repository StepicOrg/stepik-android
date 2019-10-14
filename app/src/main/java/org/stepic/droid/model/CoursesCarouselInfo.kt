package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import java.util.Arrays

data class CoursesCarouselInfo(
    val colorType: CoursesCarouselColorType,
    val title: String,
    val courseListType: CourseListType?, //if null -> see courseIds:LongArray
    val courseIds: LongArray?,
    val description: String = ""
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readParcelable<CoursesCarouselColorType>(CoursesCarouselColorType::class.java.classLoader)!!,
        source.readString()!!,
        CourseListType.values().getOrNull(source.readInt()),
        source.createLongArray(),
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(colorType, flags)
        writeString(title)
        writeInt(courseListType?.ordinal ?: -1)
        writeLongArray(courseIds)
        writeString(description)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<CoursesCarouselInfo> = object : Parcelable.Creator<CoursesCarouselInfo> {
            override fun createFromParcel(source: Parcel): CoursesCarouselInfo = CoursesCarouselInfo(source)
            override fun newArray(size: Int): Array<CoursesCarouselInfo?> = arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoursesCarouselInfo

        if (colorType != other.colorType) return false
        if (title != other.title) return false
        if (courseListType != other.courseListType) return false
        if (!Arrays.equals(courseIds, other.courseIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = colorType.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (courseListType?.hashCode() ?: 0)
        result = 31 * result + (courseIds?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }
}
