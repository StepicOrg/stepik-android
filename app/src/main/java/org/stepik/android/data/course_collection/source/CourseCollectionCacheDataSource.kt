package org.stepik.android.data.course_collection.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.CourseCollection

interface CourseCollectionCacheDataSource {
    fun getCourseCollectionList(lang: String): Single<List<CourseCollection>>

    fun replaceCourseCollectionList(lang: String, items: List<CourseCollection>): Completable
}