package org.stepic.droid.util

import org.junit.Test
import org.stepic.droid.notifications.model.Notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class HtmlHelperTest {
    @Test
    fun testNotificationModuleOpened() {
        val htmlRaw = " В курсе <a href=\"/course/%D0%A8%D0%BA%D0%BE%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F-%D1%84%D0%B8%D0%B7%D0%B8%D0%BA%D0%B0-%D0%A2%D0%B5%D0%BF%D0%BB%D0%BE%D0%B2%D1%8B%D0%B5-%D0%B8-%D1%8D%D0%BB%D0%B5%D0%BA%D1%82%D1%80%D0%BE%D0%BC%D0%B0%D0%B3%D0%BD%D0%B8%D1%82%D0%BD%D1%8B%D0%B5-%D1%8F%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F-432/\">Школьная физика. Тепловые и электромагнитные явления.</a> открылся новый модуль <a href=\"/course/%D0%A8%D0%BA%D0%BE%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F-%D1%84%D0%B8%D0%B7%D0%B8%D0%BA%D0%B0-%D0%A2%D0%B5%D0%BF%D0%BB%D0%BE%D0%B2%D1%8B%D0%B5-%D0%B8-%D1%8D%D0%BB%D0%B5%D0%BA%D1%82%D1%80%D0%BE%D0%BC%D0%B0%D0%B3%D0%BD%D0%B8%D1%82%D0%BD%D1%8B%D0%B5-%D1%8F%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F-432/\">Постоянный ток</a> "
        predictCourseIdByHtml(432L, htmlRaw)
    }

    @Test
    fun testNotificationSoftDeadline() {
        val htmlRaw = " В курсе <a href=\"/course/%D0%A8%D0%BA%D0%BE%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F-%D1%84%D0%B8%D0%B7%D0%B8%D0%BA%D0%B0-%D0%A2%D0%B5%D0%BF%D0%BB%D0%BE%D0%B2%D1%8B%D0%B5-%D0%B8-%D1%8D%D0%BB%D0%B5%D0%BA%D1%82%D1%80%D0%BE%D0%BC%D0%B0%D0%B3%D0%BD%D0%B8%D1%82%D0%BD%D1%8B%D0%B5-%D1%8F%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F-432/\">Школьная физика. Тепловые и электромагнитные явления.</a> менее чем через 36 часов наступит крайний срок сдачи заданий по модулю <a href=\"/course/%D0%A8%D0%BA%D0%BE%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F-%D1%84%D0%B8%D0%B7%D0%B8%D0%BA%D0%B0-%D0%A2%D0%B5%D0%BF%D0%BB%D0%BE%D0%B2%D1%8B%D0%B5-%D0%B8-%D1%8D%D0%BB%D0%B5%D0%BA%D1%82%D1%80%D0%BE%D0%BC%D0%B0%D0%B3%D0%BD%D0%B8%D1%82%D0%BD%D1%8B%D0%B5-%D1%8F%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F-432/\">Тепловые явления</a> "
        predictCourseIdByHtml(432L, htmlRaw)
    }

    @Test
    fun testNotificationHardDeadline() {
        val htmlRaw = " В курсе <a href=\"/course/Web-%D1%82%D0%B5%D1%85%D0%BD%D0%BE%D0%BB%D0%BE%D0%B3%D0%B8%D0%B8-154/\">Web технологии</a> менее чем через 36 часов наступит совсем крайний срок сдачи заданий по модулю <a href=\"/course/Web-%D1%82%D0%B5%D1%85%D0%BD%D0%BE%D0%BB%D0%BE%D0%B3%D0%B8%D0%B8-154/\">Дополнительные темы</a> "
         predictCourseIdByHtml(154L, htmlRaw)
    }

    private fun predictCourseIdByHtml(courseId : Long, htmlRaw: String){
        val notification = buildNotificationWithHtml(htmlRaw)
        val id = HtmlHelper.parseCourseIdFromNotification(notification)
        assertNotNull(id)
        assertEquals(id!!.toLong(), courseId)
    }

    private fun buildNotificationWithHtml(htmlRaw: String): Notification {
        val notification = Notification()
        notification.htmlText = htmlRaw
        return notification
    }
}
