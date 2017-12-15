package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.model.ViewedNotification
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureViewedNotificationsQueue
import javax.inject.Inject


class ViewedNotificationsQueueDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations): DaoBase<ViewedNotification>(databaseOperations) {
    override fun getDbName(): String =
            DbStructureViewedNotificationsQueue.VIEWED_NOTIFICATIONS_QUEUE

    override fun getDefaultPrimaryColumn(): String =
            DbStructureViewedNotificationsQueue.Column.NOTIFICATION_ID

    override fun getDefaultPrimaryValue(persistentObject: ViewedNotification): String =
            persistentObject.notificationId.toString()

    override fun getContentValues(persistentObject: ViewedNotification): ContentValues =
            ContentValues().apply {
                put(defaultPrimaryColumn, persistentObject.notificationId)
            }

    override fun parsePersistentObject(cursor: Cursor): ViewedNotification =
            ViewedNotification(cursor.getLong(cursor.getColumnIndex(defaultPrimaryColumn)))
}