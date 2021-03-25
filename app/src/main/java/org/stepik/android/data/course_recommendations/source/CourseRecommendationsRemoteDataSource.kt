package org.stepik.android.data.course_recommendations.source

import io.reactivex.Maybe
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation

interface CourseRecommendationsRemoteDataSource {
    fun getCourseRecommendations(language: String): Maybe<List<CourseRecommendation>>
}