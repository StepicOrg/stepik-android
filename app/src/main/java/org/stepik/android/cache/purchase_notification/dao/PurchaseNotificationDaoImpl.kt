package org.stepik.android.cache.purchase_notification.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.getLong
import org.stepik.android.cache.purchase_notification.structure.DbStructurePurchaseNotification
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled
import javax.inject.Inject

class PurchaseNotificationDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations) : DaoBase<PurchaseNotificationScheduled>(databaseOperations), PurchaseNotificationDao {
    override fun getDefaultPrimaryColumn(): String =
        DbStructurePurchaseNotification.Columns.COURSE_ID

    override fun getDbName(): String =
        DbStructurePurchaseNotification.TABLE_NAME

    override fun getDefaultPrimaryValue(persistentObject: PurchaseNotificationScheduled): String =
        persistentObject.courseId.toString()

    override fun getContentValues(persistentObject: PurchaseNotificationScheduled): ContentValues =
        ContentValues(2).apply {
            put(DbStructurePurchaseNotification.Columns.COURSE_ID, persistentObject.courseId)
            put(DbStructurePurchaseNotification.Columns.SCHEDULED_TIME, persistentObject.scheduledTime)
        }

    override fun parsePersistentObject(cursor: Cursor): PurchaseNotificationScheduled =
        PurchaseNotificationScheduled(
            cursor.getLong(DbStructurePurchaseNotification.Columns.COURSE_ID),
            cursor.getLong(DbStructurePurchaseNotification.Columns.SCHEDULED_TIME)
        )

    override fun getClosestScheduledNotification(): PurchaseNotificationScheduled? =
        rawQuery("SELECT * FROM $dbName WHERE ${DbStructurePurchaseNotification.Columns.SCHEDULED_TIME} > ${DateTimeHelper.nowUtc()} " +
        "ORDER BY ${DbStructurePurchaseNotification.Columns.SCHEDULED_TIME} LIMIT 1", null) {
            return@rawQuery if (it.moveToFirst()) {
                parsePersistentObject(it)
            } else {
                null
            }
        }
}