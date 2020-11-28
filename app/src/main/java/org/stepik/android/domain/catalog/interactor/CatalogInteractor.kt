package org.stepik.android.domain.catalog.interactor

import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.domain.catalog_block.repository.CatalogBlockRepository
import org.stepik.android.domain.course_collection.model.CourseCollectionQuery
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CatalogInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val courseCollectionRepository: CourseCollectionRepository,
    private val catalogBlockRepository: CatalogBlockRepository
) {
    fun fetchCourseCollections(): Single<List<CourseCollection>> =
        courseCollectionRepository
            .getCourseCollections(CourseCollectionQuery(language = sharedPreferenceHelper.languageForFeatured))

    // TODO Testing API, remove later
    fun fetchCatalogBlocks(): Single<List<CatalogBlockItem>> =
        catalogBlockRepository
            .getCatalogBlocks(primarySourceType = DataSourceType.REMOTE)
}