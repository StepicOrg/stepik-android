package org.stepik.android.remote.magic_links.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.magic_links.model.MagicLink

class MagicLinksResponse(
    @SerializedName("magic-links")
    val magicLinks: List<MagicLink>
)