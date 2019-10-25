package org.stepik.android.model.attempts

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

data class Dataset(
    val options: List<String>? = null,
    val someStringValueFromServer: String? = null,
    val pairs: List<Pair>? = null,
    val rows: List<String>? = null,
    val columns: List<String>? = null,
    val description: String? = null,

    @SerializedName("is_multiple_choice")
    val isMultipleChoice: Boolean = false,
    @SerializedName("is_checkbox")
    val isCheckbox: Boolean = false,
    @SerializedName("is_html_enabled")
    val isHtmlEnabled: Boolean = false
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(options)
        parcel.writeString(someStringValueFromServer)
        parcel.writeTypedList(pairs)
        parcel.writeStringList(rows)
        parcel.writeStringList(columns)
        parcel.writeString(description)
        parcel.writeBoolean(isMultipleChoice)
        parcel.writeBoolean(isCheckbox)
        parcel.writeBoolean(isHtmlEnabled)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Dataset> {
        override fun createFromParcel(parcel: Parcel): Dataset =
            Dataset(
                parcel.createStringArrayList(),
                parcel.readString(),
                parcel.createTypedArrayList(Pair),
                parcel.createStringArrayList(),
                parcel.createStringArrayList(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean()
            )

        override fun newArray(size: Int): Array<Dataset?> =
            arrayOfNulls(size)
    }
}

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