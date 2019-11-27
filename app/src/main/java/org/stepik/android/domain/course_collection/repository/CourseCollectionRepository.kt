package org.stepik.android.domain.course_collection.repository

import io.reactivex.Single
import org.stepik.android.model.CourseCollection

interface CourseCollectionRepository {
    fun getCourseCollection(lang: String): Single<List<CourseCollection>>
}