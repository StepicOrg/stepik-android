package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import org.stepic.droid.storage.operations.Table

data class CoursesCarouselInfo(
        val colorType: CoursesCarouselColorType,
        val title: String,
        val table: Table?, //if null -> see courseIds:LongArray
        val courseIds: LongArray?
) : Parcelable {

    constructor(source: Parcel) : this(
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
}