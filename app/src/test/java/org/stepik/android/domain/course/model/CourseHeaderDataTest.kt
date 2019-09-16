package org.stepik.android.domain.course.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.checkout.Sku
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepik.android.domain.billing.model.SkuSerializableWrapper
import org.stepik.android.model.Course
import org.stepik.android.model.Progress

@RunWith(RobolectricTestRunner::class)
class CourseHeaderDataTest {

    @Test
    fun courseHeaderDataIsParcelable() {
        val sku = Sku(
            "prod", "code", "price",
            Sku.Price(0, "USD"),
            "title", "description", "introductoryPrice",
            Sku.Price(0, "USD"),
            "subscriptionPeriod", "freeTrialPeriod", "introductoryPricePeriod", 0)


        val courseHeaderData = CourseHeaderData(
            courseId = 100,
            course = Course(id = 100),
            title = "title",
            cover = "cover",
            learnersCount = 100,
            review = 1.0,
            progress = Progress(),
            readiness = 1.0,
            enrollmentState = EnrollmentState.NotEnrolledInApp(SkuSerializableWrapper(sku))
        )

        courseHeaderData.assertThatObjectParcelable<CourseHeaderData>()
    }
}