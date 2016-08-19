package org.stepic.droid.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilTest {
    @Test
    fun testUriForCourse_correct (){
        //todo: provide IConfig
        val  expected ="https://stepik.org/course/Школьная-физика-Тепловые-и-электромагнитные-явления-432/"
        assertEquals("only for stepik instance (production): ", expected, StringUtil.getUriForCourse("https://stepik.org","Школьная-физика-Тепловые-и-электромагнитные-явления-432"))
    }

}
//https://stepik.org/course/Школьная-физика-Тепловые-и-электромагнитные-явления-432/
