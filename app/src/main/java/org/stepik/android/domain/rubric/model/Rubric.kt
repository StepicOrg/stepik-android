package org.stepik.android.domain.rubric.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Rubric(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,
    @SerializedName("instruction")
    val instruction: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("cost")
    val cost: Int,
    @SerializedName("position")
    val position: Int
) : Parcelable
