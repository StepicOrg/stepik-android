package org.stepik.android.data.course_list.source

import io.reactivex.Single
import org.stepik.android.remote.course_list.model.CourseCollectionsResponse

interface CourseListRemoteDataSource {
    fun getCourseCollections(language: String): Single<CourseCollectionsResponse>
}