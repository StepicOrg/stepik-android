package org.stepik.android.data.course_collection.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_collection.model.CourseCollectionQuery
import org.stepik.android.model.CourseCollection

interface CourseCollectionCacheDataSource {
    fun getCourseCollections(ids: List<Long>): Single<List<CourseCollection>>
    fun getCourseCollections(query: CourseCollectionQuery): Single<List<CourseCollection>>

    fun replaceCourseCollections(query: CourseCollectionQuery, items: List<CourseCollection>): Completable

    fun saveCourseCollections(items: List<CourseCollection>): Completable
}