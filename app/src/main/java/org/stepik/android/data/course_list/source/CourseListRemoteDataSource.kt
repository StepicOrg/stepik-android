package org.stepik.android.data.course_list.source

import io.reactivex.Single
import org.stepic.droid.web.CourseCollectionsResponse

interface CourseListRemoteDataSource {
    fun getCourseCollections(language: String): Single<CourseCollectionsResponse>
}