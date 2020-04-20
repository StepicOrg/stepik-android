package org.stepik.android.domain.course.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepik.android.model.Course
import org.stepik.android.model.Progress

@RunWith(RobolectricTestRunner::class)
class CourseHeaderDataTest {

    @Test
    fun courseHeaderDataIsParcelable() {
//        val sku = Sku(
//            "prod", "code", "price",
//            Sku.Price(0, "USD"),
//            "title", "description", "introductoryPrice",
//            Sku.Price(0, "USD"),
//            "subscriptionPeriod", "freeTrialPeriod", "introductoryPricePeriod", 0)


        val courseHeaderData = CourseHeaderData(
            courseId = 100,
            course = Course(id = 100),
            title = "title",
            cover = "cover",
            stats = CourseStats(
                review = 1.0,
                learnersCount = 100,
                readiness = 1.0,
                progress = Progress(),
                enrollmentState = EnrollmentState.NotEnrolledWeb
            ),
            localSubmissionsCount = 5
        )

        courseHeaderData.assertThatObjectParcelable<CourseHeaderData>()
    }
}