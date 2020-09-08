package org.stepik.android.cache.review_session.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.stepik.android.domain.review_session.model.ReviewSession
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface ReviewSessionDao {
    @Query("SELECT * FROM ReviewSession WHERE id = :id")
    fun getReviewSession(id: Long): Single<ReviewSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReviewSession(item: ReviewSession): Completable
}