package org.stepik.android.domain.personal_offers.model

import com.google.gson.annotations.SerializedName

data class PersonalOffers(
    @SerializedName("promo_stories")
    val promoStories: List<Long>?
)