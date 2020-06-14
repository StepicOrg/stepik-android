package org.stepik.android.data.course_collection.source

import io.reactivex.Single
import org.stepik.android.model.CourseCollection

interface CourseCollectionRemoteDataSource {
    fun getCourseCollectionList(lang: String): Single<List<CourseCollection>>
}