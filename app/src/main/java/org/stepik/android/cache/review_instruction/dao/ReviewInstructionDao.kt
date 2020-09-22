package org.stepik.android.cache.review_instruction.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.domain.review_instruction.model.ReviewInstruction

@Dao
interface ReviewInstructionDao {
    @Query("SELECT * FROM ReviewInstruction WHERE id = :id")
    fun getReviewInstruction(id: Long): Maybe<ReviewInstruction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReviewInstruction(item: ReviewInstruction): Completable
}