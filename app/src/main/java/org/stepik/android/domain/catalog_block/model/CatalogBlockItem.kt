package org.stepik.android.domain.catalog_block.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class CatalogBlockItem(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,
    @SerializedName("position")
    val position: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("appearance")
    val appearance: String,
    @SerializedName("is_title_visible")
    val isTitleVisible: Boolean,
    @SerializedName("content")
    val content: CatalogBlockContent
)