package org.stepik.android.cache.exam_session.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.exam_session.model.ExamSession

@Dao
interface ExamSessionDao {
    @Query("SELECT * FROM ExamSession WHERE id IN (:ids)")
    fun getExamSessions(ids: List<Long>): Single<List<ExamSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveExamSessions(items: List<ExamSession>): Completable
}