package org.stepik.android.cache.rubric.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.stepik.android.domain.rubric.model.Rubric

@Dao
interface RubricDao {
    @Query("SELECT * FROM Rubric WHERE id IN (:ids)")
    fun getRubrics(ids: List<Long>): List<Rubric>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRubrics(items: List<Rubric>)
}