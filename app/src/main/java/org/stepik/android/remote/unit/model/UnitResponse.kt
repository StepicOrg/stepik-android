package org.stepik.android.remote.unit.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Unit
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class UnitResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("units")
    val units: List<Unit>
) : MetaResponse