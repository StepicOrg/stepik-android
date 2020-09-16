package org.stepik.android.cache.course_payments.structure

object DbStructureCoursePayments {
    const val TABLE_NAME = "course_payments"

    object Columns {
        const val ID = "id"
        const val COURSE = "course"
        const val IS_PAID = "is_paid"
        const val STATUS = "status"
        const val USER = "user"
    }

    const val TABLE_SCHEMA =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${Columns.ID} LONG PRIMARY KEY," +
            "${Columns.COURSE} LONG," +
            "${Columns.IS_PAID} INTEGER," +
            "${Columns.STATUS} INTEGER," +
            "${Columns.USER} LONG" +
        ")"
}