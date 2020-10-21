package org.stepik.android.domain.course_list.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import ru.nobird.android.core.model.mapOfNotNull
import java.io.Serializable

@Parcelize
data class CourseListQuery(
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("order")
    val order: Order? = null,
    @SerializedName("teacher")
    val teacher: Long? = null,

    @SerializedName("is_exclude_ended")
    val isExcludeEnded: Boolean? = null,
    @SerializedName("is_public")
    val isPublic: Boolean? = null,
    @SerializedName("is_cataloged")
    val isCataloged: Boolean? = null,
    @SerializedName("filter_query")
    val filterQuery: CourseListFilterQuery? = null
) : Parcelable, Serializable {
    companion object {
        private const val PAGE = "page"
        private const val ORDER = "order"
        private const val TEACHER = "teacher"
        private const val LANGUAGE = "language"
        private const val IS_PUBLIC = "is_public"
        private const val IS_EXCLUDE_ENDED = "exclude_ended"
        private const val IS_CATALOGED = "is_cataloged"
        private const val IS_PAID = "is_paid"
        private const val WITH_CERTIFICATE = "with_certificate"
    }

    enum class Order(val order: String) {
        @SerializedName("-activity")
        ACTIVITY_DESC("-activity"),
        @SerializedName("-popularity")
        POPULARITY_DESC("-popularity")
    }

    fun toMap(): Map<String, Any> =
        mapOfNotNull(
            PAGE to page,
            ORDER to order,
            TEACHER to teacher,
            LANGUAGE to filterQuery?.language,
            IS_PUBLIC to isPublic,
            IS_EXCLUDE_ENDED to isExcludeEnded,
            IS_CATALOGED to isCataloged,
            IS_PAID to filterQuery?.isPaid,
            WITH_CERTIFICATE to filterQuery?.withCertificate
        )
}