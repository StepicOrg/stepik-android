package org.stepic.droid.util

import org.junit.Assert.*
import org.junit.Test
import org.stepic.droid.notifications.model.Notification

class HtmlHelperTest{

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

    @Test
    fun testCourseById_InvalidHtmlText_Null(){
        val htmlRaw = "<---->"
        val notification = Notification()
        notification.htmlText = htmlRaw
        val id = HtmlHelper.parseCourseIdFromNotification(notification)
        assertNull(id)
    }

    @Test
    fun testParseIdFromSlugValidFull (){
        val correctSlug = "Школьная-физика-Тепловые-и-электромагнитные-явления-432"
        val correctId = 432L

        assertEquals(correctId, HtmlHelper.parseIdFromSlug(correctSlug))
    }

    @Test
    fun testParseIdFromOneDash (){
        val correctSlug = "-432"
        val correctId = 432L

        assertEquals(correctId, HtmlHelper.parseIdFromSlug(correctSlug))
    }

    @Test
    fun testParseIdWithoutDash (){
        val correctSlug = "432"
        val correctId = 432L

        assertEquals(correctId, HtmlHelper.parseIdFromSlug(correctSlug))
    }


    @Test
    fun testParseIdEncodedSlug(){
        val correctSlug = "%D0%A8%D0%BA%D0%BE%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F-%D1%84%D0%B8%D0%B7%D0%B8%D0%BA%D0%B0-%D0%A2%D0%B5%D0%BF%D0%BB%D0%BE%D0%B2%D1%8B%D0%B5-%D0%B8-%D1%8D%D0%BB%D0%B5%D0%BA%D1%82%D1%80%D0%BE%D0%BC%D0%B0%D0%B3%D0%BD%D0%B8%D1%82%D0%BD%D1%8B%D0%B5-%D1%8F%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F-432"
        val correctId = 432L

        assertEquals(correctId, HtmlHelper.parseIdFromSlug(correctSlug))
    }

    @Test
    fun testParseIdNumberSlug(){
        val correctSlug = "8-800-555-35-35-745"
        val correctId = 745L

        assertEquals(correctId, HtmlHelper.parseIdFromSlug(correctSlug))
    }

    //fixme init yandexmetrica with context, move to instrumental?
//    @Test
//    fun testParseIdIncorrectSlug(){
//        val correctSlug = "Course-Name"
//
//        assertNull(HtmlHelper.parseIdFromSlug(correctSlug))
//    }

    @Test
    fun testParseModulePosition(){
        val text = """ В курсе <a href="/course/%D0%9D%D0%B0%D1%83%D1%87%D0%BD%D0%BE%D0%B5-%D0%BC%D1%8B%D1%88%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-578/">Научное мышление</a> открылся новый модуль <a href="/course/%D0%9D%D0%B0%D1%83%D1%87%D0%BD%D0%BE%D0%B5-%D0%BC%D1%8B%D1%88%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-578/syllabus?module=10">Научная этика</a>  """
        val correctPosition = 10

        assertEquals(correctPosition, HtmlHelper.parseModulePositionFromNotification(text))
    }


    @Test
    fun testParseModulePositionWithOneDigest(){
        val text = """ В курсе <a href="/course/%D0%9D%D0%B0%D1%83%D1%87%D0%BD%D0%BE%D0%B5-%D0%BC%D1%8B%D1%88%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-578/">Научное мышление</a> открылся новый модуль <a href="/course/%D0%9D%D0%B0%D1%83%D1%87%D0%BD%D0%BE%D0%B5-%D0%BC%D1%8B%D1%88%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-578/syllabus?module=3">Научная этика</a>  """
        val correctPosition = 3

        assertEquals(correctPosition, HtmlHelper.parseModulePositionFromNotification(text))
    }


    private fun predictCourseIdByHtml(courseId : Long, htmlRaw: String){
        val notification = buildNotificationWithHtml(htmlRaw)
        val id = HtmlHelper.parseCourseIdFromNotification(notification)
        assertEquals(id ?:-1, courseId)
        assertNotNull(id)
    }

    private fun buildNotificationWithHtml(htmlRaw: String): Notification {
        val notification = Notification()
        notification.htmlText = htmlRaw
        return notification
    }
}
