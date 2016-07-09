package org.stepic.droid.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilTest {
    @Test
    fun testUriForCourse_correct (){
        //todo: provide IConfig
        val  expected ="https://stepic.org/course/Школьная-физика-Тепловые-и-электромагнитные-явления-432/"
        assertEquals("only for stepic instance (production): ", expected, StringUtil.getUriForCourse("https://stepic.org","Школьная-физика-Тепловые-и-электромагнитные-явления-432"))
    }

//    @Test
//    fun testGetAppUriForAppIndexing_correct(){
//        val actual = StringUtil.getAppUriForCourse("https://stepic.org", "Программирование-на-Python-67").toString()
//        val expected = "android-app://org.stepic.droid/https/stepic.org/course/Программирование-на-Python-67/"
//        assertEquals(expected, actual)
//    }

}
//https://stepic.org/course/Школьная-физика-Тепловые-и-электромагнитные-явления-432/
