package org.stepik.android.cache.visited_courses.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.stepik.android.domain.visited_courses.model.VisitedCourse

@Dao
interface VisitedCourseDao {
    @Query("SELECT * FROM VisitedCourse")
    fun getVisitedCourses(): List<VisitedCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveVisitedCourse(visitedCourses: List<VisitedCourse>)
}