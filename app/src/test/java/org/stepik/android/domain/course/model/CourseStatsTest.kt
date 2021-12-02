package org.stepik.android.domain.course.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Progress
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class CourseStatsTest {

    @Test
    fun courseStatsEnrolledIsParcelable() {
        val courseStats = createCourseStats(
            EnrollmentState.Enrolled(
                userCourse = UserCourse(
                    id = 1000,
                    user = 2000,
                    course = 3000,
                    lastViewed = Date(100000000)
                )
            )
        )

        courseStats.assertThatObjectParcelable<CourseStats>()
    }

    @Test
    fun courseStatsNotEnrolledIsParcelable() {
        val courseStats = createCourseStats(EnrollmentState.NotEnrolledFree)
        courseStats.assertThatObjectParcelable<CourseStats>()
    }

    private fun createCourseStats(enrollmentState: EnrollmentState): CourseStats =
        CourseStats(
            review = 0.4,
            learnersCount = 110,
            readiness = 0.5,
            progress = Progress(
                id = "Some id",
                lastViewed = "lastViewed",
                score = "score",
                cost = 100,
                nSteps = 101,
                nStepsPassed = 102,
                isPassed = true
            ),
            enrollmentState = enrollmentState
        )
}