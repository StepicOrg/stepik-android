package org.stepic.droid.storage.operations

import android.os.Parcel
import android.os.Parcelable
import org.stepic.droid.storage.structure.DbStructureEnrolledAndFeaturedCourses

enum class Table(val storeName: String) : Parcelable {
    enrolled(DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES),
    featured(DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES);

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
    }

    companion object {
        val CREATOR: Parcelable.Creator<Table> = object : Parcelable.Creator<Table> {
            override fun createFromParcel(source: Parcel): Table =
                    Table.values()[source.readInt()]

            override fun newArray(size: Int): Array<Table?> = arrayOfNulls(size)
        }
    }

}