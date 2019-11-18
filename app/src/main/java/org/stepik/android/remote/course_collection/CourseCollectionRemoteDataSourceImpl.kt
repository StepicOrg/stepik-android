package org.stepik.android.remote.course_collection

import io.reactivex.Single
import org.stepik.android.data.course_collection.source.CourseCollectionRemoteDataSource
import org.stepik.android.model.CourseCollection
import org.stepik.android.remote.course_collection.model.CourseCollectionsResponse
import org.stepik.android.remote.course_collection.service.CourseCollectionService
import javax.inject.Inject

class CourseCollectionRemoteDataSourceImpl
@Inject
constructor(
    private val courseCollectionService: CourseCollectionService
) : CourseCollectionRemoteDataSource {
    override fun getCourseCollectionList(lang: String): Single<List<CourseCollection>> =
        courseCollectionService.getCourseCollectionList(lang).map(CourseCollectionsResponse::courseCollections)
}