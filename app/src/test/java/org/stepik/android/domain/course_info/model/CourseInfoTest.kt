package org.stepik.android.domain.course_info.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepic.droid.testUtils.generators.FakeUserGenerator
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import org.stepik.android.view.video_player.model.VideoPlayerMediaData

@RunWith(RobolectricTestRunner::class)
class CourseInfoTest {
    companion object {
        fun createTestCourseInfoData(): CourseInfoData =
            CourseInfoData(
                organization = FakeUserGenerator.generate(),
                videoMediaData = VideoPlayerMediaData(
                    thumbnail = "",
                    title = "Title",
                    description = "Description",
                    cachedVideo = Video(
                        id = 123,
                        thumbnail = "thumbnail",
                        urls = listOf(VideoUrl("url1", "360"), VideoUrl("url2", "720")),
                        duration = 1001
                    ),
                    externalVideo = Video(
                        id = 123,
                        thumbnail = "thumbnail",
                        urls = listOf(VideoUrl("url1", "360"), VideoUrl("url2", "720")),
                        duration = 1001
                    )
                ),
                about = "About",
                requirements = "",
                targetAudience = "Testers",
                timeToComplete = 1200,
                instructors = listOf(FakeUserGenerator.generate(), FakeUserGenerator.generate()),
                language = "ru",
                certificate = CourseInfoData.Certificate(
                    title = "Certificate",
                    distinctionThreshold = 100,
                    regularThreshold = 60
                ),
                learnersCount = 100
            )
    }

    @Test
    fun courseInfoDataIsSerializable() {
        createTestCourseInfoData()
            .assertThatObjectParcelable<CourseInfoData>()
    }
}