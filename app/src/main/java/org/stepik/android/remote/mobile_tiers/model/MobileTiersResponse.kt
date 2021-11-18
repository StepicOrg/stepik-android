package org.stepik.android.remote.mobile_tiers.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.mobile_tiers.model.MobileTier

class MobileTiersResponse(
    @SerializedName("mobile-tiers")
    val mobileTiers: List<MobileTier>
)