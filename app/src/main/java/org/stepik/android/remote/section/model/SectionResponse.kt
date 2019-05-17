package org.stepik.android.remote.section.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Section
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class SectionResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("sections")
    val sections: List<Section>
) : MetaResponse
