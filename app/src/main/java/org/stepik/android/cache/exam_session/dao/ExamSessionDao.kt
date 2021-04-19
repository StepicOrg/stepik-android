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
    @Query("SELECT * FROM ExamSession where id = :id")
    fun getExamSessions(id: Long): Single<ExamSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveExamSessions(item: ExamSession): Completable
}