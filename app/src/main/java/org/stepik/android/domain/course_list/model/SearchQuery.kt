package org.stepik.android.domain.course_list.model

import android.os.Parcel
import android.os.Parcelable

data class SearchQuery(
    val page: Int? = null,
    val tag: Int? = null,
    val query: String? = null,
    val lang: String? = null
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(page)
        parcel.writeValue(tag)
        parcel.writeValue(query)
        parcel.writeValue(lang)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SearchQuery> {
        override fun createFromParcel(parcel: Parcel): SearchQuery =
            SearchQuery(
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(String::class.java.classLoader) as String?,
                parcel.readValue(String::class.java.classLoader) as String?
            )

        override fun newArray(size: Int): Array<SearchQuery?> =
            arrayOfNulls(size)
    }
}