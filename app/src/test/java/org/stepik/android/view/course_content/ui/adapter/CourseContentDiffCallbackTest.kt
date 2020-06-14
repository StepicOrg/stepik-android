package org.stepik.android.view.course_content.ui.adapter

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.Section
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import java.util.*

@RunWith(RobolectricTestRunner::class)
class CourseContentDiffCallbackTest {

    @Test
    fun sectionItemsTest() {
        val section = Section(id = 0)
        val dates = emptyList<CourseContentSectionDate>()

        val sectionItemA = CourseContentItem.SectionItem(section, dates, null, true)

        val sectionItemB = CourseContentItem.SectionItem(section, listOf(CourseContentSectionDate(0, Date())), null, true)

        val callback = CourseContentDiffCallback(listOf(sectionItemA), listOf(sectionItemB))

        assertEquals(true, callback.areItemsTheSame(0, 0))
        assertEquals(false, callback.areContentsTheSame(0, 0))
    }

}