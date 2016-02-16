package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.operations.ResultHandler;
import org.stepic.droid.util.RWLocks;

public abstract class DaoBase {
    protected SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    protected Context mContext;


    public DaoBase(Context context) {
        mContext = context;
        dbHelper = new DatabaseHelper(context);
    }

    private void open() {
        RWLocks.DatabaseLock.writeLock().lock();
        database = dbHelper.getWritableDatabase();
    }

    private void close() {
        database.close();
        RWLocks.DatabaseLock.writeLock().unlock();
    }

    protected <T> T executeQuery(String sqlQuery, String[] selectionArgs, ResultHandler<T> handler) {
        try {
            open();
            Cursor cursor = database.rawQuery(sqlQuery, selectionArgs);
            T result = handler.handle(cursor);
            cursor.close();
            return result;
        } finally {
            close();
        }

    }

    protected void executeUpdate(String table, ContentValues values, String whereClause, String[] whereArgs) {
        try {
            open();
            database.update(table, values, whereClause, whereArgs);
        } finally {
            close();
        }
    }

    protected void executeInsert(String table, ContentValues values) {
        try {
            open();
            database.insert(table, null, values);
        } finally {
            close();
        }
    }

    protected void insertOrUpdate(String tableName, ContentValues cv, String primaryKeyColumn, String primaryValue) {
        if (isInDb(tableName, primaryKeyColumn, primaryValue)) {
            String whereClause = primaryKeyColumn + "=?";
            String[] whereArgs = new String[]{primaryValue};
            executeUpdate(tableName, cv, whereClause, whereArgs);
        } else {
            executeInsert(tableName, cv);
        }
    }

    protected boolean isInDb(String tableName, String column, String columnValue) {
        String Query = "Select * from " + tableName + " where " + column + " = ?";
        return executeQuery(Query, new String[]{columnValue}, new ResultHandler<Boolean>() {
            @Override
            public Boolean handle(Cursor cursor) throws SQLException {
                if (cursor.getCount() <= 0) {
                    cursor.close();
                    return false;
                } else {
                    return true;
                }
            }
        });
    }


}
