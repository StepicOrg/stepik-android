package org.stepik.android.domain.filter.model

import com.google.gson.annotations.SerializedName
import ru.nobird.android.core.model.mapOfNotNull

data class SubmissionsFilterQuery(
    @SerializedName("page")
    val page: Int? = null,
    @SerializedName("user")
    val user: Long? = null,
    @SerializedName("order")
    val order: Order? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("review_status")
    val reviewStatus: ReviewStatus? = null,
    @SerializedName("search")
    val search: String? = null
) {
    companion object {
        private const val PAGE = "page"
        private const val USER = "user"
        private const val ORDER = "order"
        private const val STATUS = "status"
        private const val REVIEW_STATUS = "review_status"
        private const val SEARCH = "search"

        val DEFAULT_QUERY = SubmissionsFilterQuery(page = 1, order = Order.DESC)
    }
    enum class Order(val order: String) {
        @SerializedName("desc")
        DESC("desc"),
        @SerializedName("asc")
        ASC("asc")
    }

    enum class ReviewStatus(val reviewStatus: String) {
        @SerializedName("awaiting")
        AWAITING("awaiting"),
        @SerializedName("done")
        DONE("done")
    }

    fun toMap(): Map<String, String> =
        mapOfNotNull(
            PAGE to page?.toString(),
            USER to user?.toString(),
            STATUS to status,
            ORDER to order?.order,
            REVIEW_STATUS to reviewStatus?.reviewStatus,
            SEARCH to search
        )
}
