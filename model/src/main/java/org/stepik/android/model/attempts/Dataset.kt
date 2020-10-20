package org.stepik.android.model.attempts

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Dataset(
    val options: List<String>? = null,
    val someStringValueFromServer: String? = null,
    val pairs: List<Pair>? = null,
    val rows: List<String>? = null,
    val columns: List<String>? = null,
    val description: String? = null,
    val components: List<Component>? = null,

    @SerializedName("is_multiple_choice")
    val isMultipleChoice: Boolean = false,
    @SerializedName("is_checkbox")
    val isCheckbox: Boolean = false,
    @SerializedName("is_html_enabled")
    val isHtmlEnabled: Boolean = false
) : Parcelable

data class DatasetWrapper(
    val dataset: Dataset? = null
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(dataset, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DatasetWrapper> {
        override fun createFromParcel(parcel: Parcel): DatasetWrapper =
            DatasetWrapper(parcel.readParcelable(Dataset::class.java.classLoader))

        override fun newArray(size: Int): Array<DatasetWrapper?> =
            arrayOfNulls(size)
    }
}