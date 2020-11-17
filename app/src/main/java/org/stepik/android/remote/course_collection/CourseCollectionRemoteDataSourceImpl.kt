package org.stepik.android.remote.course_collection

import io.reactivex.Single
import org.stepik.android.data.course_collection.source.CourseCollectionRemoteDataSource
import org.stepik.android.domain.course_collection.model.CourseCollectionQuery
import org.stepik.android.model.CourseCollection
import org.stepik.android.remote.course_collection.model.CourseCollectionsResponse
import org.stepik.android.remote.course_collection.service.CourseCollectionService
import javax.inject.Inject

class CourseCollectionRemoteDataSourceImpl
@Inject
constructor(
    private val courseCollectionService: CourseCollectionService
) : CourseCollectionRemoteDataSource {
    override fun getCourseCollections(ids: List<Long>): Single<List<CourseCollection>> =
        courseCollectionService
            .getCourseCollections(ids)
            .map(CourseCollectionsResponse::courseCollections)

    override fun getCourseCollections(query: CourseCollectionQuery): Single<List<CourseCollection>> =
        courseCollectionService
            .getCourseCollections(query.toMap())
            .map(CourseCollectionsResponse::courseCollections)
}