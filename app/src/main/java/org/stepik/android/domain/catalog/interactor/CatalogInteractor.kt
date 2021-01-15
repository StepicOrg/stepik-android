package org.stepik.android.domain.catalog.interactor

import io.reactivex.Maybe
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog.model.CatalogBlock
import org.stepik.android.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class CatalogInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val catalogRepository: CatalogRepository
) {
    fun fetchCatalogBlocks(): Maybe<List<CatalogBlock>> =
        catalogRepository
            .getCatalogBlocks(language = sharedPreferenceHelper.languageForFeatured, primarySourceType = DataSourceType.REMOTE)
}