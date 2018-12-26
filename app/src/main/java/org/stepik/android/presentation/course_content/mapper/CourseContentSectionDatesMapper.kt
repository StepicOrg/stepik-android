package org.stepik.android.presentation.course_content.mapper

import org.stepic.droid.R
import org.stepik.android.model.Section
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import javax.inject.Inject

class CourseContentSectionDatesMapper
@Inject
constructor() {

    fun mapSectionDates(section: Section): List<CourseContentSectionDate> =
        listOfNotNull(
            section.beginDate?.let    { CourseContentSectionDate(R.string.course_content_timeline_begin_date, it) },
            section.softDeadline?.let { CourseContentSectionDate(R.string.course_content_timeline_soft_deadline, it) },
            section.hardDeadline?.let { CourseContentSectionDate(R.string.course_content_timeline_hard_deadline, it) },
            section.endDate?.let      { CourseContentSectionDate(R.string.course_content_timeline_end_date, it) }
        )

}