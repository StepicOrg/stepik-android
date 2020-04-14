package org.stepik.android.domain.catalog.interactor

import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CatalogInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val courseCollectionRepository: CourseCollectionRepository
) {
    fun fetchCourseCollections(): Single<List<CourseCollection>> =
        courseCollectionRepository
            .getCourseCollection(sharedPreferenceHelper.languageForFeatured)
}