package org.stepik.android.cache.review_session.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.stepik.android.domain.review_session.model.ReviewSession

@Dao
interface ReviewSessionDao {
    @Query("SELECT * FROM ReviewSession WHERE id IN (:ids)")
    fun getReviewSessions(ids: List<Long>): List<ReviewSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReviewSession(item: ReviewSession)
}