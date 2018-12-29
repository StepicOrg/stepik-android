package org.stepik.android.view.course.routing

import android.content.Intent
import android.net.Uri
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CourseDeepLinkHandlerTest {

    @Test
    fun defaultDeepLinkTest() {
        val intent = Intent()
        intent.data = Uri.parse("https://stepik.org/course/67")

        Assert.assertEquals(67L, intent.getCourseIdFromDeepLink())
    }

    @Test
    fun slugDeepLinkTest() {
        val intent = Intent()
        intent.data = Uri.parse("https://stepik.org/course/Школьная-физика-Тепловые-и-электромагнитные-явления-432")

        Assert.assertEquals(432L, intent.getCourseIdFromDeepLink())
    }

    @Test
    fun syllabusDeepLinkTest() {
        val intent = Intent()
        intent.data = Uri.parse("https://stepik.org/course/Школьная-физика-Тепловые-и-электромагнитные-явления-432/syllabus")

        Assert.assertEquals(432L, intent.getCourseIdFromDeepLink())
    }

    @Test
    fun syllabusTabDeepLinkTest() {
        val intent = Intent()
        intent.data = Uri.parse("https://stepik.org/course/Школьная-физика-Тепловые-и-электромагнитные-явления-432/syllabus")

        Assert.assertEquals(CourseScreenTab.SYLLABUS, intent.getCourseTabFromDeepLink())
    }

}
