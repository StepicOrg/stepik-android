package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TableChoiceAnswer(
    @SerializedName("name_row")
    val nameRow: String,
    @SerializedName("columns")
    val columns: List<Cell>
) : Parcelable