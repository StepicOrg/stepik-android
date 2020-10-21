package org.stepik.android.domain.course_list.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.filter.model.CourseListFilterQuery
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
    val filterQuery: CourseListFilterQuery? = null
) : Parcelable, Serializable {
    enum class Order(val order: String) {
        @SerializedName("-activity")
        ACTIVITY_DESC("-activity"),
        @SerializedName("-popularity")
        POPULARITY_DESC("-popularity")
    }
}