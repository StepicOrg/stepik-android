package org.stepik.android.cache.proctor_session.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.proctor_session.model.ProctorSession

@Dao
interface ProctorSessionDao {
    @Query("SELECT * FROM ProctorSession where id = :id")
    fun getProctorSessions(id: Long): Single<ProctorSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProctorSessions(item: ProctorSession): Completable
}