package org.stepik.android.cache.course_recommendations.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation

@Dao
interface CourseRecommendationsDao {
    @Query("SELECT * FROM CourseRecommendation")
    fun getCourseRecommendations(): Single<List<CourseRecommendation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourseRecommendations(courseRecommendations: List<CourseRecommendation>): Completable

    @Query("DELETE FROM CourseRecommendation")
    fun clearCourseRecommendations(): Completable
}