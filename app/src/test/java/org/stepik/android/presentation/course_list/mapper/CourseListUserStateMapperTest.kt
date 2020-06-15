package org.stepik.android.presentation.course_list.mapper

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CourseListUserStateMapperTest {

    @Test
    fun test() {
        val mapper = CourseListUserStateMapper()

        mapper.mergeUserCourses()
    }

}