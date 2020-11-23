package org.stepik.android.domain.catalog_block.model

import com.google.gson.annotations.SerializedName
import java.util.*

class SpecializationCatalogBlockContentItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("details_url")
    val detailsUrl: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("discount")
    val discount: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("start_date")
    val startDate: Date?,
    @SerializedName("end_date")
    val endDate: Date?,
    @SerializedName("duration")
    val duration: String
) : CatalogBlockContentItem