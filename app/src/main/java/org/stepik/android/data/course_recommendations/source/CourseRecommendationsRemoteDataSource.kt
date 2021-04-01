package org.stepik.android.data.course_recommendations.source

import io.reactivex.Single
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation

interface CourseRecommendationsRemoteDataSource {
    fun getCourseRecommendations(language: String): Single<List<CourseRecommendation>>
}