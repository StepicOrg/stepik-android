package org.stepik.android.cache.base.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.stepik.android.cache.analytic.converter.JsonElementConverter
import org.stepik.android.cache.analytic.dao.AnalyticDao
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent

@Database(entities = [AnalyticLocalEvent::class], version = AnalyticDatabaseInfo.DATABASE_VERSION, exportSchema = false)
@TypeConverters(JsonElementConverter::class)
abstract class AnalyticDatabase : RoomDatabase() {
    abstract fun analyticDao(): AnalyticDao
}