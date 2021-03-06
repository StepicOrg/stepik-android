package org.stepik.android.domain.course_recommendations.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation

interface CourseRecommendationsRepository {
    fun getCourseRecommendations(
        language: String,
        primarySourceType: DataSourceType = DataSourceType.REMOTE
    ): Single<List<CourseRecommendation>>
}