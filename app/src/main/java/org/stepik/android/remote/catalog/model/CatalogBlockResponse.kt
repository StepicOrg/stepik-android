package org.stepik.android.remote.catalog.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.catalog.model.CatalogBlock
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CatalogBlockResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("catalog-blocks")
    val catalogBlocks: List<CatalogBlock>
) : MetaResponse