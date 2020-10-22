package org.stepik.android.domain.filter.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.nobird.android.core.model.mapOfNotNull
import java.io.Serializable

@Parcelize
data class CourseListFilterQuery(
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("is_paid")
    val isPaid: Boolean? = null,
    @SerializedName("with_certificate")
    val withCertificate: Boolean? = null
) : Parcelable, Serializable {
    companion object {
        private const val LANGUAGE = "language"
        private const val IS_PAID = "is_paid"
        private const val WITH_CERTIFICATE = "with_certificate"
    }

    fun toMap(): Map<String, Any> =
        mapOfNotNull(
            LANGUAGE to language,
            IS_PAID to isPaid,
            WITH_CERTIFICATE to withCertificate
        )
}