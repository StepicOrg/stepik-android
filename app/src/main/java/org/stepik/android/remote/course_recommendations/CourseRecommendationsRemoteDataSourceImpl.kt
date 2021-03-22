package org.stepik.android.remote.course_recommendations

import io.reactivex.Maybe
import org.stepik.android.data.course_recommendations.source.CourseRecommendationsRemoteDataSource
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation
import org.stepik.android.remote.course_recommendations.model.CourseRecommendationsResponse
import org.stepik.android.remote.course_recommendations.service.CourseRecommendationsService
import javax.inject.Inject

class CourseRecommendationsRemoteDataSourceImpl
@Inject
constructor(
    private val courseRecommendationsService: CourseRecommendationsService
) : CourseRecommendationsRemoteDataSource {
    companion object {
        const val LANGUAGE_QUERY_PARAM = "language"
    }
    override fun getCourseRecommendations(language: String): Maybe<List<CourseRecommendation>> =
        courseRecommendationsService
            .getCourseRecommendations(mapOf(LANGUAGE_QUERY_PARAM to language))
            .map(CourseRecommendationsResponse::courseRecommendations)
}