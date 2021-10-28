package org.stepik.android.remote.mobile_tiers.model

import com.google.gson.annotations.SerializedName

data class MobileTierCalculation(
    @SerializedName("course")
    val course: Long,
    @SerializedName("store")
    val store: String = STORE_VALUE,
    @SerializedName("promo")
    val promo: String? = null
) {
    companion object {
        private const val STORE_VALUE = "google_play"
    }
}