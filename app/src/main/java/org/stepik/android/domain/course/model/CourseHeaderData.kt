package org.stepik.android.domain.course.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.model.Course

@Parcelize
data class CourseHeaderData(
    val courseId: Long,
    val course: Course,
    val title: String,
    val cover: String,

    val stats: CourseStats,
    val localSubmissionsCount: Int,
    val deeplinkPromoCode: DeeplinkPromoCode,
    val defaultPromoCode: DefaultPromoCode,
    val isWishlistUpdating: Boolean,
    val wishlistEntity: WishlistEntity
) : Parcelable
