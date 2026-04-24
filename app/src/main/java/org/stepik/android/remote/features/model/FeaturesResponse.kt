package org.stepik.android.remote.features.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class FeaturesResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("features")
    val features: List<Feature>
) : MetaResponse