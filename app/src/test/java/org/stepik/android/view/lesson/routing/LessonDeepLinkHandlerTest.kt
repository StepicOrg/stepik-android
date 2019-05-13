package org.stepik.android.view.lesson.routing

import android.content.Intent
import android.net.Uri
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LessonDeepLinkHandlerTest {

    @Test
    fun lessonIdParameterTest() {
        val intent = Intent()
        val actualParameter = 67L
        intent.data = Uri.parse("https://stepik.org/lesson/$actualParameter")

        Assert.assertEquals(actualParameter, intent.getLessonIdFromDeepLink())
    }

    @Test
    fun stepPositionParameterTest() {
        val intent = Intent()
        val actualParameter = 67L
        intent.data = Uri.parse("https://stepik.org/lesson/123/step/$actualParameter")

        Assert.assertEquals(actualParameter, intent.getStepPositionFromDeepLink())
    }

    @Test
    fun noStepPositionParameterTest() {
        val intent = Intent()
        intent.data = Uri.parse("https://stepik.org/lesson/123/")

        Assert.assertEquals(null, intent.getStepPositionFromDeepLink())
    }

    @Test
    fun unitIdParameterTest() {
        val intent = Intent()
        val actualParameter = 67L
        intent.data = Uri.parse("https://stepik.org/lesson/123/step/11?unit=$actualParameter")

        Assert.assertEquals(actualParameter, intent.getUnitIdFromDeepLink())
    }

    @Test
    fun noUnitIdParameterTest() {
        val intent = Intent()
        intent.data = Uri.parse("https://stepik.org/lesson/123/step/11")

        Assert.assertEquals(null, intent.getUnitIdFromDeepLink())
    }

    @Test
    fun discussionIdParameterTest() {
        val intent = Intent()
        val actualParameter = 67L
        intent.data = Uri.parse("https://stepik.org/lesson/123/step/11?discussion=$actualParameter")

        Assert.assertEquals(actualParameter, intent.getDiscussionIdFromDeepLink())
    }

    @Test
    fun noDiscussionIdParameterTest() {
        val intent = Intent()
        intent.data = Uri.parse("https://stepik.org/lesson/123/step/11")

        Assert.assertEquals(null, intent.getDiscussionIdFromDeepLink())
    }

    @Test
    fun completeUrlTest() {
        val intent = Intent()
        val actualLessonId = 1L
        val actualStepPosition = 2L
        val actualUnitId = 3L
        val actualDiscussionId = 4L
        intent.data = Uri.parse("https://stepik.org/lesson/$actualLessonId/step/$actualStepPosition/?unit=$actualUnitId&discussion=$actualDiscussionId")

        Assert.assertEquals(actualLessonId, intent.getLessonIdFromDeepLink())
        Assert.assertEquals(actualStepPosition, intent.getStepPositionFromDeepLink())
        Assert.assertEquals(actualUnitId, intent.getUnitIdFromDeepLink())
        Assert.assertEquals(actualDiscussionId, intent.getDiscussionIdFromDeepLink())
    }

    @Test
    fun completeLessonDataUrlTest() {
        val intent = Intent()

        val actualLessonDeepLinkData = LessonDeepLinkData(
            lessonId = 1,
            stepPosition = 2,
            unitId = 3,
            discussionId = 4
        )
        intent.data = Uri.parse(
            "https://stepik.org/lesson/${actualLessonDeepLinkData.lessonId}" +
                "/step/${actualLessonDeepLinkData.stepPosition}" +
                "/?unit=${actualLessonDeepLinkData.unitId}" +
                "&discussion=${actualLessonDeepLinkData.discussionId}"
        )

        Assert.assertEquals(actualLessonDeepLinkData, intent.getLessonDeepLinkData())
    }
}