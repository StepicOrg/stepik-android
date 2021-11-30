package org.stepik.android.presentation.course_purchase.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.model.Course

@Parcelize
data class CoursePurchaseData(
    val course: Course,
    val stats: CourseStats,
    val primarySku: LightSku,
    val promoCodeSku: PromoCodeSku,
    val isWishlisted: Boolean
) : Parcelable
