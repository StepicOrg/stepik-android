package org.stepik.android.domain.course_payments.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.mobile_tiers.model.LightSku

@Parcelize
data class PromoCodeSku(
    val name: String,
    val lightSku: LightSku?
) : Parcelable {
    companion object {
        val EMPTY = PromoCodeSku("", null)
    }
}