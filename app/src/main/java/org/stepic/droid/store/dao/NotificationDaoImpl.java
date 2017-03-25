package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.notifications.model.NotificationType;
import org.stepic.droid.store.structure.DbStructureNotification;

import javax.inject.Inject;

public class NotificationDaoImpl extends DaoBase<Notification> {

    @Inject
    public NotificationDaoImpl(SQLiteDatabase openHelper) {
        super(openHelper);
    }

    @Override
    String getDbName() {
        return DbStructureNotification.NOTIFICATIONS_TEMP;
    }

    @Override
    String getDefaultPrimaryColumn() {
        return DbStructureNotification.Column.ID;
    }

    @Override
    String getDefaultPrimaryValue(Notification persistentObject) {
        if (persistentObject == null || persistentObject.getId() == null) {
            return "0";
        }
        return persistentObject.getId().toString();
    }

    @Override
    ContentValues getContentValues(Notification persistentObject) {
        ContentValues values = new ContentValues();

        values.put(DbStructureNotification.Column.ID, persistentObject.getId());
        values.put(DbStructureNotification.Column.IS_UNREAD, persistentObject.is_unread());
        values.put(DbStructureNotification.Column.IS_MUTED, persistentObject.isMuted());
        values.put(DbStructureNotification.Column.IS_FAVOURITE, persistentObject.isFavourite());
        values.put(DbStructureNotification.Column.TIME, persistentObject.getTime());
        values.put(DbStructureNotification.Column.TYPE, persistentObject.getType().name()); //!!!
        values.put(DbStructureNotification.Column.LEVEL, persistentObject.getLevel());
        values.put(DbStructureNotification.Column.PRIORITY, persistentObject.getPriority());
        values.put(DbStructureNotification.Column.HTML_TEXT, persistentObject.getHtmlText());
        values.put(DbStructureNotification.Column.ACTION, persistentObject.getAction());
        values.put(DbStructureNotification.Column.COURSE_ID, persistentObject.getCourse_id());
        return values;
    }

    @Override
    Notification parsePersistentObject(Cursor cursor) {
        Notification notification = new Notification();

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

        notification.setId(cursor.getLong(columnIndexId));
        notification.set_unread(cursor.getInt(columnIndexIsUnread) > 0);
        notification.setMuted(cursor.getInt(columnIndexIsMuted) > 0);
        notification.setFavourite(cursor.getInt(columnIndexIsFavourite) > 0);
        notification.setTime(cursor.getString(columnIndexTime));
        notification.setType(NotificationType.valueOf(cursor.getString(columnIndexType)));//!!!
        notification.setLevel(cursor.getString(columnIndexLevel));
        notification.setPriority(cursor.getString(columnIndexPriority));
        notification.setHtmlText(cursor.getString(columnIndexHtmlText));
        notification.setAction(cursor.getString(columnIndexAction));
        notification.setCourse_id(cursor.getLong(columnIndexCourseId));

        return notification;
    }
}
