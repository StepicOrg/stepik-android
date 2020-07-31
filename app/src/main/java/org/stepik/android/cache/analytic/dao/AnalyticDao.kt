package org.stepik.android.cache.analytic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent
import org.stepik.android.cache.analytic.structure.DbStructureAnalytic

@Dao
interface AnalyticDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAnalyticEvent(analyticLocalEvent: AnalyticLocalEvent): Completable

    @Query("SELECT * FROM ${DbStructureAnalytic.TABLE_NAME}")
    fun getAllAnalyticEvents(): Single<List<AnalyticLocalEvent>>

    @Query("DELETE FROM ${DbStructureAnalytic.TABLE_NAME}")
    fun clearEvents(): Completable
}