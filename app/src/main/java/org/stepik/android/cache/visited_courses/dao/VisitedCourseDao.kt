package org.stepik.android.cache.visited_courses.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import io.reactivex.Completable
import io.reactivex.Flowable
import org.stepik.android.domain.visited_courses.model.VisitedCourse

@Dao
abstract class VisitedCourseDao {
    @Query("SELECT * FROM VisitedCourse ORDER BY id DESC LIMIT 20")
    abstract fun getVisitedCourses(): Flowable<List<VisitedCourse>>

    @Query("SELECT COALESCE(MAX(id), 0) FROM VisitedCourse")
    abstract fun getMaxId(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveVisitedCourses(visitedCourses: List<VisitedCourse>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveVisitedCourse(visitedCourse: VisitedCourse)

    @Transaction
    open fun saveVisitedCourse(courseId: Long) {
        val id = getMaxId()
        return saveVisitedCourse(VisitedCourse(id = id + 1, course = courseId))
    }
}