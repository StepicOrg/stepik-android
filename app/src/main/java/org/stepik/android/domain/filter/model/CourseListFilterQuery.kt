package org.stepik.android.domain.filter.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class CourseListFilterQuery(
    @SerializedName("lang")
    val language: String? = null,
    @SerializedName("is_paid")
    val isPaid: Boolean? = null,
    @SerializedName("with_certificate")
    val withCertificate: Boolean? = null
) : Parcelable, Serializable