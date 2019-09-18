package org.stepik.android.presentation.course_content.mapper

import org.stepic.droid.util.hasUserAccess
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.RequiredSection
import javax.inject.Inject

class CourseContentItemMapper
@Inject
constructor(
    private val sectionDatesMapper: CourseContentSectionDatesMapper
) {
    fun mapSectionsWithEmptyUnits(course: Course, sections: List<Section>, progresses: List<Progress>): List<CourseContentItem> =
        sections
            .flatMap { section ->
                mapSectionWithEmptyUnits(course, section, progresses, sections)
            }

    private fun mapSectionWithEmptyUnits(course: Course, section: Section, progresses: List<Progress>, sections: List<Section>): List<CourseContentItem> =
        listOf(CourseContentItem.SectionItem(
            section = section,
            dates = sectionDatesMapper.mapSectionDates(section),
            progress = progresses.find { it.id == section.progress },
            isEnabled = section.hasUserAccess(course),
            requiredSection = mapRequiredSection(section, sections, progresses)
        )) + section.units.map(CourseContentItem::UnitItemPlaceholder)

    private fun mapRequiredSection(section: Section, sections: List<Section>, progresses: List<Progress>): RequiredSection? =
        if (!section.isRequirementSatisfied) {
            val requiredSection = sections.find { it.id == section.requiredSection }
            val progress = progresses.find { it.id == requiredSection?.progress }

            if (requiredSection != null && progress != null) {
                RequiredSection(requiredSection, progress)
            } else {
                null
            }
        } else {
            null
        }

    fun mapUnits(sectionItems: List<CourseContentItem.SectionItem>, units: List<Unit>, lessons: List<Lesson>, progresses: List<Progress>): List<CourseContentItem.UnitItem> =
        units.mapNotNull { unit ->
            val sectionItem = sectionItems.find { it.section.id == unit.section } ?: return@mapNotNull null
            val lesson = lessons.find { it.id == unit.lesson } ?: return@mapNotNull null
            val progress = progresses.find { it.id == unit.progress }
            CourseContentItem.UnitItem(sectionItem.section, unit, lesson, progress, sectionItem.isEnabled)
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