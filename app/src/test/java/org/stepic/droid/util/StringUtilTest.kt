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

}
//https://stepic.org/course/Школьная-физика-Тепловые-и-электромагнитные-явления-432/
