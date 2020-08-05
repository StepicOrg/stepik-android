package org.stepik.android.cache.analytic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent
import org.stepik.android.cache.analytic.structure.DbStructureAnalytic

@Dao
interface AnalyticDao {
    companion object {
        private const val EVENT_LIMIT = 100
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAnalyticEvent(analyticLocalEvent: AnalyticLocalEvent): Completable

    @Query("SELECT * FROM ${DbStructureAnalytic.TABLE_NAME} LIMIT $EVENT_LIMIT")
    fun getAnalyticEvents(): Single<List<AnalyticLocalEvent>>

    @Delete
    fun clearEvents(analyticLocalEvents: List<AnalyticLocalEvent>): Completable
}