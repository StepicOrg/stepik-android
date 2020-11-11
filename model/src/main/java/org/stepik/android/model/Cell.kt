package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.nobird.android.core.model.Identifiable

@Parcelize
data class Cell(
    @SerializedName("name")
    override val id: String,
    @SerializedName("answer")
    var answer: Boolean
) : Parcelable, Identifiable<String> {
    val name: String
        get() = id
}