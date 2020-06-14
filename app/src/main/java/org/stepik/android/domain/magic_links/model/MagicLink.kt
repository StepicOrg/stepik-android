package org.stepik.android.domain.magic_links.model

import com.google.gson.annotations.SerializedName

class MagicLink(
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String
)