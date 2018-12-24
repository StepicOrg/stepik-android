package org.stepic.droid.storage.dao;

import android.content.ContentValues;
import android.database.Cursor;

import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.notifications.model.NotificationType;
import org.stepic.droid.storage.operations.DatabaseOperations;
import org.stepic.droid.storage.structure.DbStructureNotification;

import javax.inject.Inject;

public class NotificationDaoImpl extends DaoBase<Notification> {

    @Inject
    public NotificationDaoImpl(DatabaseOperations databaseOperations) {
        super(databaseOperations);
    }

    @Override
    protected String getDbName() {
        return DbStructureNotification.NOTIFICATIONS_TEMP;
    }

    @Override
    protected String getDefaultPrimaryColumn() {
        return DbStructureNotification.Column.ID;
    }

    @Override
    protected String getDefaultPrimaryValue(Notification persistentObject) {
        if (persistentObject == null || persistentObject.getId() == null) {
            return "0";
        }
        return persistentObject.getId().toString();
    }

    @Override
    protected ContentValues getContentValues(Notification persistentObject) {
        ContentValues values = new ContentValues();

        values.put(DbStructureNotification.Column.ID, persistentObject.getId());
        values.put(DbStructureNotification.Column.IS_UNREAD, persistentObject.isUnread());
        values.put(DbStructureNotification.Column.IS_MUTED, persistentObject.isMuted());
        values.put(DbStructureNotification.Column.IS_FAVOURITE, persistentObject.isFavourite());
        values.put(DbStructureNotification.Column.TIME, persistentObject.getTime());
        values.put(DbStructureNotification.Column.TYPE, persistentObject.getType().name()); //!!!
        values.put(DbStructureNotification.Column.LEVEL, persistentObject.getLevel());
        values.put(DbStructureNotification.Column.PRIORITY, persistentObject.getPriority());
        values.put(DbStructureNotification.Column.HTML_TEXT, persistentObject.getHtmlText());
        values.put(DbStructureNotification.Column.ACTION, persistentObject.getAction());
        values.put(DbStructureNotification.Column.COURSE_ID, persistentObject.getCourseId());
        return values;
    }

    @Override
    protected Notification parsePersistentObject(Cursor cursor) {
        int columnIndexId = cursor.getColumnIndex(DbStructureNotification.Column.ID);
        int columnIndexIsUnread = cursor.getColumnIndex(DbStructureNotification.Column.IS_UNREAD);
        int columnIndexIsMuted = cursor.getColumnIndex(DbStructureNotification.Column.IS_MUTED);
        int columnIndexIsFavourite = cursor.getColumnIndex(DbStructureNotification.Column.IS_FAVOURITE);
        int columnIndexTime = cursor.getColumnIndex(DbStructureNotification.Column.TIME);
        int columnIndexType = cursor.getColumnIndex(DbStructureNotification.Column.TYPE);
        int columnIndexLevel = cursor.getColumnIndex(DbStructureNotification.Column.LEVEL);
        int columnIndexPriority = cursor.getColumnIndex(DbStructureNotification.Column.PRIORITY);
        int columnIndexHtmlText = cursor.getColumnIndex(DbStructureNotification.Column.HTML_TEXT);
        int columnIndexAction = cursor.getColumnIndex(DbStructureNotification.Column.ACTION);
        int columnIndexCourseId = cursor.getColumnIndex(DbStructureNotification.Column.COURSE_ID);

        return new Notification(
                cursor.getLong(columnIndexId),
                cursor.getInt(columnIndexIsUnread) > 0,
                cursor.getInt(columnIndexIsMuted) > 0,
                cursor.getInt(columnIndexIsFavourite) > 0,
                cursor.getString(columnIndexTime),
                NotificationType.valueOf(cursor.getString(columnIndexType)),
                cursor.getString(columnIndexLevel),
                cursor.getString(columnIndexPriority),
                cursor.getString(columnIndexHtmlText),
                cursor.getString(columnIndexAction),
                cursor.getLong(columnIndexCourseId),
                null,
                null,
                0
        );
    }
}
