package org.stepik.android.view.course_content.mapper

import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import javax.inject.Inject

class CourseContentItemMapper
@Inject
constructor() {
    fun mapSectionWithEmptyUnits(section: Section, progress: Progress?): List<CourseContentItem> =
        listOf(CourseContentItem.SectionItem(section, mapSectionDates(section), progress, DownloadProgress.Status.Pending)) +
                section.units.map(CourseContentItem::UnitItemPlaceholder)

    private fun mapSectionDates(section: Section): List<CourseContentSectionDate> =
        listOfNotNull(
            section.beginDate?.let    { CourseContentSectionDate(R.string.course_content_timeline_begin_date, it) },
            section.softDeadline?.let { CourseContentSectionDate(R.string.course_content_timeline_soft_deadline, it) },
            section.hardDeadline?.let { CourseContentSectionDate(R.string.course_content_timeline_hard_deadline, it) },
            section.endDate?.let      { CourseContentSectionDate(R.string.course_content_timeline_end_date, it) }
        )
}