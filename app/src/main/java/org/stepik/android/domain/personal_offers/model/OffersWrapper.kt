package org.stepik.android.domain.personal_offers.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OffersWrapper(
    @SerializedName("promo_stories")
    val promoStories: List<Long>?
) : Parcelable