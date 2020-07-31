package org.stepik.android.cache.analytic.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonElement
import org.stepik.android.cache.analytic.structure.DbStructureAnalytic

@Entity(tableName = DbStructureAnalytic.TABLE_NAME)
data class AnalyticLocalEvent(
    @ColumnInfo(name = DbStructureAnalytic.Columns.EVENT_NAME)
    val name: String,
    @ColumnInfo(name = DbStructureAnalytic.Columns.EVENT_JSON)
    val eventData: JsonElement,
    @ColumnInfo(name = DbStructureAnalytic.Columns.EVENT_TIMESTAMP)
    val eventTimestamp: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}