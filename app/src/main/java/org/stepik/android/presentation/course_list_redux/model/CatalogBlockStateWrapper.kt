package org.stepik.android.presentation.course_list_redux.model

import org.stepik.android.domain.catalog.model.CatalogBlock
import org.stepik.android.domain.catalog.model.CatalogBlockContent.Companion.AUTHORS
import org.stepik.android.domain.catalog.model.CatalogBlockContent
import org.stepik.android.domain.catalog.model.CatalogBlockContent.Companion.FULL_COURSE_LISTS
import org.stepik.android.domain.catalog.model.CatalogBlockContent.Companion.RECOMMENDED_COURSES
import org.stepik.android.domain.catalog.model.CatalogBlockContent.Companion.SIMPLE_COURSE_LISTS
import org.stepik.android.domain.catalog.model.CatalogBlockContent.Companion.SPECIALIZATIONS
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import ru.nobird.app.core.model.Identifiable

sealed class CatalogBlockStateWrapper : Identifiable<String> {
    data class FullCourseList(
        val catalogBlock: CatalogBlock,
        val state: CourseListFeature.State
    ) : CatalogBlockStateWrapper() {
        override val id: String =
            "$FULL_COURSE_LISTS${catalogBlock.id}"
    }

    data class SimpleCourseListsDefault(
        val catalogBlockItem: CatalogBlock,
        val content: CatalogBlockContent.SimpleCourseLists
    ) : CatalogBlockStateWrapper() {
        override val id: String =
            "$SIMPLE_COURSE_LISTS${catalogBlockItem.id}"
    }

    data class SimpleCourseListsGrid(
        val catalogBlockItem: CatalogBlock,
        val content: CatalogBlockContent.SimpleCourseLists
    ) : CatalogBlockStateWrapper() {
        override val id: String =
            "$SIMPLE_COURSE_LISTS${catalogBlockItem.id}"
    }

    data class AuthorList(
        val catalogBlockItem: CatalogBlock,
        val content: CatalogBlockContent.AuthorsList
    ) : CatalogBlockStateWrapper() {
        override val id: String = "${AUTHORS}${catalogBlockItem.id}"
    }

    data class RecommendedCourseList(
        val catalogBlockItem: CatalogBlock,
        val state: CourseListFeature.State
    ) : CatalogBlockStateWrapper() {
        override val id: String
            get() = "${RECOMMENDED_COURSES}${catalogBlockItem.id}"
    }

    data class SpecializationList(
        val catalogBlockItem: CatalogBlock,
        val content: CatalogBlockContent.SpecializationsList
    ) : CatalogBlockStateWrapper() {
        override val id: String
            get() = "${SPECIALIZATIONS}${catalogBlockItem.id}"
    }
}