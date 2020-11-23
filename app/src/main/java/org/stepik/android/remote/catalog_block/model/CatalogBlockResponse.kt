package org.stepik.android.remote.catalog_block.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CatalogBlockResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("catalog-blocks")
    val catalogBlocks: List<CatalogBlockItem>
) : MetaResponse