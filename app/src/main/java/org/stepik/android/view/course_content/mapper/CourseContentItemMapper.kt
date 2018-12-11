package org.stepik.android.view.course_content.mapper

import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import javax.inject.Inject

class CourseContentItemMapper
@Inject
constructor() {
    fun mapSectionsWithEmptyUnits(sections: List<Section>, progresses: List<Progress>): List<CourseContentItem> =
        sections
            .flatMap { section ->
                mapSectionWithEmptyUnits(section, progresses.find { it.id == section.progress })
            }

    fun mapSectionWithEmptyUnits(section: Section, progress: Progress?): List<CourseContentItem> =
        listOf(CourseContentItem.SectionItem(section, mapSectionDates(section), progress, DownloadProgress.Status.NotCached)) +
                section.units.map(CourseContentItem::UnitItemPlaceholder)

    private fun mapSectionDates(section: Section): List<CourseContentSectionDate> =
        listOfNotNull(
            section.beginDate?.let    { CourseContentSectionDate(R.string.course_content_timeline_begin_date, it) },
            section.softDeadline?.let { CourseContentSectionDate(R.string.course_content_timeline_soft_deadline, it) },
            section.hardDeadline?.let { CourseContentSectionDate(R.string.course_content_timeline_hard_deadline, it) },
            section.endDate?.let      { CourseContentSectionDate(R.string.course_content_timeline_end_date, it) }
        )

    fun mapUnits(units: List<Unit>, sections: List<Section>, progresses: List<Progress>, lessons: List<Lesson>): List<CourseContentItem.UnitItem> =
        units.mapNotNull { unit ->
            val section = sections.find { it.id == unit.section } ?: return@mapNotNull null
            val lesson = lessons.find { it.id == unit.lesson } ?: return@mapNotNull null
            val progress = progresses.find { it.id == unit.progress }
            CourseContentItem.UnitItem(section, unit, lesson, progress, DownloadProgress.Status.NotCached)
        }

    fun replaceUnitPlaceholders(items: List<CourseContentItem>, unitItems: List<CourseContentItem.UnitItem>): List<CourseContentItem> =
        items.map { item ->
            (item as? CourseContentItem.UnitItemPlaceholder)
                ?.let { unitItems.find { unitItem -> it.unitId == unitItem.unit.id } }
                ?: item
        }

    fun getUnitPlaceholdersIds(items: List<CourseContentItem>): LongArray =
        items
            .filterIsInstance<CourseContentItem.UnitItemPlaceholder>()
            .map(CourseContentItem.UnitItemPlaceholder::unitId)
            .toLongArray()
}