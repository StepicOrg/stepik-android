package org.stepik.android.domain.course.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.model.Course
import org.stepik.android.model.Progress

@RunWith(RobolectricTestRunner::class)
class CourseHeaderDataTest {

    @Test
    fun courseHeaderDataIsParcelable() {
        val courseHeaderData = CourseHeaderData(
            courseId = 100,
            course = Course(id = 100),
            title = "title",
            cover = "cover",
            stats = CourseStats(
                review = 1.0,
                learnersCount = 100,
                readiness = 1.0,
                progress = Progress(id = "1"),
                enrollmentState = EnrollmentState.NotEnrolledWeb
            ),
            localSubmissionsCount = 5,
            deeplinkPromoCode = DeeplinkPromoCode("CODE", "200", "RUB"),
            deeplinkPromoCodeSku = PromoCodeSku("CODE", LightSku("price_tier_2", "1 899,00 ₽")),
            defaultPromoCode = DefaultPromoCode.EMPTY,
            isWishlistUpdating = false
        )

        courseHeaderData.assertThatObjectParcelable<CourseHeaderData>()
    }
}