package org.stepik.android.cache.course_payments.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getBoolean
import org.stepic.droid.util.getInt
import org.stepic.droid.util.getLong
import org.stepik.android.cache.course_payments.structure.DbStructureCoursePayments
import org.stepik.android.domain.course_payments.model.CoursePayment
import javax.inject.Inject

class CoursePaymentsDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<CoursePayment>(databaseOperations) {
    override fun getDbName(): String =
        DbStructureCoursePayments.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        DbStructureCoursePayments.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: CoursePayment): String =
        persistentObject.id.toString()

    override fun parsePersistentObject(cursor: Cursor): CoursePayment =
        CoursePayment(
            id = cursor.getLong(DbStructureCoursePayments.Columns.ID),
            course = cursor.getLong(DbStructureCoursePayments.Columns.COURSE),
            isPaid = cursor.getBoolean(DbStructureCoursePayments.Columns.IS_PAID),
            status = CoursePayment.Status.values()[cursor.getInt(DbStructureCoursePayments.Columns.STATUS)],
            user = cursor.getLong(DbStructureCoursePayments.Columns.USER)
        )

    override fun getContentValues(persistentObject: CoursePayment): ContentValues =
        ContentValues().apply {
            put(DbStructureCoursePayments.Columns.ID, persistentObject.id)
            put(DbStructureCoursePayments.Columns.COURSE, persistentObject.course)
            put(DbStructureCoursePayments.Columns.IS_PAID, persistentObject.isPaid)
            put(DbStructureCoursePayments.Columns.STATUS, persistentObject.status.ordinal)
            put(DbStructureCoursePayments.Columns.USER, persistentObject.user)
        }
}