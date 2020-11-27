package org.stepik.android.remote.course_collection

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.course_collection.source.CourseCollectionRemoteDataSource
import org.stepik.android.domain.course_collection.model.CourseCollectionQuery
import org.stepik.android.model.CourseCollection
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.course_collection.model.CourseCollectionsResponse
import org.stepik.android.remote.course_collection.service.CourseCollectionService
import javax.inject.Inject

class CourseCollectionRemoteDataSourceImpl
@Inject
constructor(
    private val courseCollectionService: CourseCollectionService
) : CourseCollectionRemoteDataSource {
    private val mapper =
        Function<CourseCollectionsResponse, List<CourseCollection>>(CourseCollectionsResponse::courseCollections)

    override fun getCourseCollections(ids: List<Long>): Single<List<CourseCollection>> =
        ids.chunkedSingleMap {
            courseCollectionService
                .getCourseCollections(it)
                .map(mapper)
        }

    override fun getCourseCollections(query: CourseCollectionQuery): Single<List<CourseCollection>> =
        courseCollectionService
            .getCourseCollections(query.toMap())
            .map(mapper)
}