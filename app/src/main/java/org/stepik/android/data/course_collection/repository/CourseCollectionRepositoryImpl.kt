package org.stepik.android.data.course_collection.repository

import io.reactivex.Single
import org.stepik.android.data.course_collection.source.CourseCollectionRemoteDataSource
import org.stepik.android.domain.course_collection.repository.CourseCollectionRepository
import org.stepik.android.model.CourseCollection
import javax.inject.Inject

class CourseCollectionRepositoryImpl
@Inject
constructor(
    private val courseCollectionRemoteDataSource: CourseCollectionRemoteDataSource
) : CourseCollectionRepository {
    override fun getCourseCollection(lang: String): Single<List<CourseCollection>> =
        courseCollectionRemoteDataSource
            .getCourseCollectionList(lang)
            .map { it.sortedBy(CourseCollection::position) }
}