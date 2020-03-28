package org.stepik.android.domain.search_result.model

import android.os.Parcel
import android.os.Parcelable

data class SearchResultQuery(
    val page: Int? = null,
    val tagId: Int? = null,
    val query: String? = null,
    val lang: String? = null
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(page)
        parcel.writeValue(tagId)
        parcel.writeValue(query)
        parcel.writeValue(lang)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SearchResultQuery> {
        override fun createFromParcel(parcel: Parcel): SearchResultQuery =
            SearchResultQuery(
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(String::class.java.classLoader) as String?,
                parcel.readValue(String::class.java.classLoader) as String?
            )

        override fun newArray(size: Int): Array<SearchResultQuery?> =
            arrayOfNulls(size)
    }
}