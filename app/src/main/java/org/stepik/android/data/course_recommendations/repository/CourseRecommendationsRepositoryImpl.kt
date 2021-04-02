package org.stepik.android.data.course_recommendations.repository

import io.reactivex.Single
import org.stepik.android.data.course_recommendations.source.CourseRecommendationsCacheDataSource
import org.stepik.android.data.course_recommendations.source.CourseRecommendationsRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation
import org.stepik.android.domain.course_recommendations.repository.CourseRecommendationsRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class CourseRecommendationsRepositoryImpl
@Inject
constructor(
    private val courseRecommendationsRemoteDataSource: CourseRecommendationsRemoteDataSource,
    private val courseRecommendationsCacheDataSource: CourseRecommendationsCacheDataSource
) : CourseRecommendationsRepository {
    override fun getCourseRecommendations(language: String, primarySourceType: DataSourceType): Single<List<CourseRecommendation>> {
        val remoteSource = courseRecommendationsRemoteDataSource
            .getCourseRecommendations(language)
            .doCompletableOnSuccess { courseRecommendationsCacheDataSource.saveCourseRecommendations(it) }

        val cacheDataSource = courseRecommendationsCacheDataSource
            .getCourseRecommendations()

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource
                    .onErrorResumeNext(cacheDataSource)

            DataSourceType.CACHE ->
                cacheDataSource
                    .filter(List<CourseRecommendation>::isNotEmpty)
                    .switchIfEmpty(remoteSource)

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }
}