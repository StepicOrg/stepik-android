package org.stepik.android.presentation.course_list_redux.model

import org.stepik.android.domain.catalog_block.model.CatalogBlockContent.Companion.FULL_COURSE_LISTS
import org.stepik.android.domain.catalog_block.model.CatalogBlock
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import ru.nobird.android.core.model.Identifiable

sealed class CatalogBlockStateWrapper : Identifiable<String> {
    data class CourseList(
        val catalogBlock: CatalogBlock,
        val state: CourseListFeature.State
    ) : CatalogBlockStateWrapper() {
        override val id: String =
            "$FULL_COURSE_LISTS${catalogBlock.id}"
    }
}