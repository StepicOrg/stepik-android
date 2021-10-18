package org.stepik.android.presentation.course_purchase.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.model.Course

@Parcelize
data class CoursePurchaseData(
    val course: Course,
    val stats: CourseStats,
    val deeplinkPromoCode: DeeplinkPromoCode,
    val defaultPromoCode: DefaultPromoCode,
    val wishlistEntity: WishlistEntity,
    val isWishlisted: Boolean
) : Parcelable
