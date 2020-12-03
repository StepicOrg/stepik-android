package org.stepik.android.presentation.course_list_redux.model

import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import ru.nobird.android.core.model.Identifiable

sealed class CatalogBlockStateWrapper : Identifiable<Long> {
    data class CourseList(val catalogBlockItem: CatalogBlockItem, val state: CourseListFeature.State) : CatalogBlockStateWrapper() {
        override val id: Long = catalogBlockItem.id
    }
}