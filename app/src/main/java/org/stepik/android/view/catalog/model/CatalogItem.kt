package org.stepik.android.view.catalog.model

import org.stepik.android.domain.banner.model.Banner
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.presentation.stories.StoriesFeature
import ru.nobird.app.core.model.Identifiable

sealed class CatalogItem {
    data class Stories(val state: StoriesFeature.State) : CatalogItem(), Identifiable<String> {
        override val id: String = "stories"
    }

    data class Filters(val state: FiltersFeature.State) : CatalogItem(), Identifiable<String> {
        override val id: String = "filters"
    }

    object Offline : CatalogItem(), Identifiable<String> {
        override val id: String = "offline"
    }

    object Loading : CatalogItem(), Identifiable<String> {
        override val id: String = "loading"
    }

    data class Block(val catalogBlockStateWrapper: CatalogBlockStateWrapper) : CatalogItem(), Identifiable<String> {
        override val id: String = "block_${catalogBlockStateWrapper.id}"
    }

    data class BannerBlock(val banner: Banner) : CatalogItem(), Identifiable<String> {
        override val id: String = "banner_block_${banner.position}"
    }
}