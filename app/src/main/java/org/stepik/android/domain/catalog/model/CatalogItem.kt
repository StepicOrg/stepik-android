package org.stepik.android.domain.catalog.model

sealed class CatalogItem {
    object StoriesItem : CatalogItem()
}