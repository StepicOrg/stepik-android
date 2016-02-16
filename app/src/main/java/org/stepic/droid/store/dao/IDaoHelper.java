package org.stepic.droid.store.dao;

import android.content.ContentValues;

public interface IDaoHelper {
    boolean isInDb(String tableName, String column, String columnValue);

    void insertOrUpdate(String tableName, ContentValues cv, String primaryKeyColumn, String primaryValue);
}
