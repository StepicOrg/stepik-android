package org.stepik.android.cache.purchase_notification.structure

object DbStructurePurchaseNotification {
    const val TABLE_NAME = "purchase_notification"

    object Columns {
        const val COURSE_ID = "course_id"
        const val SCHEDULED_TIME = "scheduled_time"
    }

    const val TABLE_SCHEMA =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${Columns.COURSE_ID} LONG PRIMARY KEY," +
            "${Columns.SCHEDULED_TIME} LONG" +
        ")"
}