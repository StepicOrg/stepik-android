package org.stepik.android.view.catalog_block.model

import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.presentation.stories.StoriesFeature
import ru.nobird.android.core.model.Identifiable

sealed class CatalogBlockItem {
    data class StoriesBlock(val state: StoriesFeature.State) : CatalogBlockItem(), Identifiable<String> {
        override val id: String
            get() = "stories"
    }

    data class FiltersBlock(val state: FiltersFeature.State) : CatalogBlockItem(), Identifiable<String> {
        override val id: String
            get() = "filters"
    }
}