package org.stepik.android.data.course_recommendations.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation

interface CourseRecommendationsCacheDataSource {
    fun getCourseRecommendations(): Single<List<CourseRecommendation>>
    fun saveCourseRecommendations(courseRecommendations: List<CourseRecommendation>): Completable
}