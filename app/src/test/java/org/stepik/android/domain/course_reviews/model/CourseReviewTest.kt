package org.stepik.android.domain.course_reviews.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class CourseReviewTest {

    @Test
    fun courseReviewIsParcelable() {
        val courseReview = CourseReview(
            id = 10,
            course = 124,
            user = 1312312,
            score = 22,
            text = "Some text",
            createDate = Date(23423),
            updateDate = Date(2131203021)
        )
        courseReview.assertThatObjectParcelable<CourseReview>()
    }
}