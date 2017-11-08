package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import org.stepic.droid.storage.operations.Table
import java.util.*

data class CoursesCarouselInfo(
        val colorType: CoursesCarouselColorType,
        val title: String,
        val table: Table?, //if null -> see courseIds:LongArray
        val courseIds: LongArray?,
        val description: String = ""
) : Parcelable {

    private constructor(source: Parcel) : this(
            source.readParcelable<CoursesCarouselColorType>(CoursesCarouselColorType::class.java.classLoader),
            source.readString(),
            source.readParcelable<Table>(Table::class.java.classLoader),
            source.createLongArray()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(colorType, flags)
        writeString(title)
        writeParcelable(table, flags)
        writeLongArray(courseIds)
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
        if (table != other.table) return false
        if (!Arrays.equals(courseIds, other.courseIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = colorType.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (table?.hashCode() ?: 0)
        result = 31 * result + (courseIds?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }
}
