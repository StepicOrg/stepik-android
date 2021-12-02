package org.stepik.android.remote.wishlist.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.wishlist.model.WishlistEntry
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class WishlistResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("wish-lists")
    val wishlistEntries: List<WishlistEntry>
) : MetaResponse