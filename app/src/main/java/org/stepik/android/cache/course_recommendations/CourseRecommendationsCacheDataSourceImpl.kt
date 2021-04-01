package org.stepik.android.cache.course_recommendations

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.course_recommendations.dao.CourseRecommendationsDao
import org.stepik.android.data.course_recommendations.source.CourseRecommendationsCacheDataSource
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation
import javax.inject.Inject

class CourseRecommendationsCacheDataSourceImpl
@Inject
constructor(
    private val courseRecommendationsDao: CourseRecommendationsDao
) : CourseRecommendationsCacheDataSource {
    override fun getCourseRecommendations(): Single<List<CourseRecommendation>> =
        courseRecommendationsDao.getCourseRecommendations()

    override fun saveCourseRecommendations(courseRecommendations: List<CourseRecommendation>): Completable =
        courseRecommendationsDao
            .clearCourseRecommendations()
            .andThen(courseRecommendationsDao.insertCourseRecommendations(courseRecommendations))
}