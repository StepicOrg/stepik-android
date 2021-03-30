package org.stepik.android.domain.course_recommendations.interactor

import io.reactivex.Maybe
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation
import org.stepik.android.domain.course_recommendations.repository.CourseRecommendationsRepository
import javax.inject.Inject

class CourseRecommendationsInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val courseRecommendationsRepository: CourseRecommendationsRepository
) {
    fun fetchCourseRecommendations(sourceType: DataSourceType = DataSourceType.REMOTE): Maybe<List<CourseRecommendation>> =
        courseRecommendationsRepository
            .getCourseRecommendations(language = sharedPreferenceHelper.languageForFeatured, sourceType)
}