package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class CourseTest {
    companion object {
        fun createTestCourse(): Course =
            Course(
                id = 5888,
                title = "Test course",
                description = "Test description",
                cover = "/media/images/course.png",
                certificate = "",
                requirements = "requirements",
                summary = "summary",
                workload = "",
                intro = "",
                introVideo = Video(
                    id = 123,
                    thumbnail = "thumbnail",
                    urls = listOf(VideoUrl("url1", "360"), VideoUrl("url2", "720")),
                    duration = 1001
                ),
                language = "ru",
                authors = listOf(338348, 342324, 98534),
                instructors = listOf(338348, 342324, 98534),
                sections = listOf(12344, 12345, 12346, 12347),
                courseFormat = "",
                targetAudience = "For testers",
                certificateFooter = "",
                certificateCoverOrg = "/media/images/img.png",
                totalUnits = 60,
                enrollment = 0,
                progress = "",
                owner = 23123,
                readiness = 0.0,
                isContest = false,
                isFeatured = false,
                isActive = false,
                isPublic = false,
                isArchived = false,
                isFavorite = false,
                certificateDistinctionThreshold = 0,
                certificateRegularThreshold = 0,
                certificateLink = "",
                isCertificateAutoIssued = false,
                isCertificateIssued = false,
                lastDeadline = "",
                beginDate = "",
                endDate = "",
                slug = "",
                scheduleLink = "",
                scheduleLongLink = "",
                scheduleType = "",
                lastStepId = "",
                learnersCount = 0,
                reviewSummary = 0,
                timeToComplete = 0,
                courseOptions = CourseOptions(
                    CoursePreview(
                        1L
                    )
                ),
                isPaid = false,
                price = "",
                currencyCode = "",
                displayPrice = "",
                priceTier = ""
            )
    }

    @Test
    fun courseIsSerializable() {
        createTestCourse()
            .assertThatObjectParcelable<Course>()
    }
}