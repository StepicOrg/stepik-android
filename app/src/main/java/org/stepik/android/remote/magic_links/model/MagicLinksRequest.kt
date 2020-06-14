package org.stepik.android.remote.magic_links.model

import com.google.gson.annotations.SerializedName

class MagicLinksRequest(
    @SerializedName("magic-link")
    val magicLink: Body
) {
    class Body(
        @SerializedName("next_url")
        val nextUrl: String
    )
}