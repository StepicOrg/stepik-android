package org.stepik.android.domain.course_payments.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepic.droid.util.DateTimeHelper
import java.util.Date

@Parcelize
data class DefaultPromoCode(
    val defaultPromoCodeName: String,
    val defaultPromoCodePrice: String,
    val defaultPromoCodeDiscount: String,
    val defaultPromoCodeExpireDate: Date?
) : Parcelable {
    val isPromoCodeValid: Boolean
        get() = defaultPromoCodeExpireDate?.time ?: -1L > DateTimeHelper.nowUtc()

    companion object {
        val EMPTY = DefaultPromoCode("", "", "", null)
    }
}
