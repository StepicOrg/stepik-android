package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.store.operations.ResultHandler;
import org.stepic.droid.util.RWLocks;

import java.util.ArrayList;
import java.util.List;

public abstract class DaoBase<T> implements IDao<T> {

    private SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;

    public DaoBase(SQLiteOpenHelper openHelper) {
        dbHelper = openHelper;
    }

    private void open() {
        RWLocks.DatabaseLock.writeLock().lock();
        database = dbHelper.getWritableDatabase();
    }

    private void close() {
        database.close();
        RWLocks.DatabaseLock.writeLock().unlock();
    }

    private <U> U executeQuery(String sqlQuery, String[] selectionArgs, ResultHandler<U> handler) {
        try {
            open();
            Cursor cursor = database.rawQuery(sqlQuery, selectionArgs);
            U result = handler.handle(cursor);
            cursor.close();
            return result;
        } finally {
            close();
        }

    }

    private void executeUpdate(String table, ContentValues values, String whereClause, String[] whereArgs) {
        try {
            open();
            database.update(table, values, whereClause, whereArgs);
        } finally {
            close();
        }
    }

    private void executeInsert(String table, ContentValues values) {
        try {
            open();
            database.insert(table, null, values);
        } finally {
            close();
        }
    }

    private void insertOrUpdate(String tableName, ContentValues cv, String primaryKeyColumn, String primaryValue) {
        if (isInDb(tableName, primaryKeyColumn, primaryValue)) {
            String whereClause = primaryKeyColumn + "=?";
            String[] whereArgs = new String[]{primaryValue};
            executeUpdate(tableName, cv, whereClause, whereArgs);
        } else {
            executeInsert(tableName, cv);
        }
    }

    @NotNull
    @Override
    public List<T> getAll() {
        String query = "Select * from " + getDbName();
        return getAllWithQuery(query, null);
    }

    @Override
    @NotNull
    public List<T> getAll(String whereColumn, String whereValue) {
        String query = "Select * from " + getDbName() + " where " + whereColumn + " = ?";
        return getAllWithQuery(query, new String[]{whereValue});
    }

    @Override
    @Nullable
    public T get(String whereColumn, String whereValue) {
        String query = "Select * from " + getDbName() + " where " + whereColumn + " = ?";
        return executeQuery(query, new String[]{whereValue}, new ResultHandler<T>() {
            @Override
            public T handle(Cursor cursor) throws SQLException {
                cursor.moveToFirst();

                if (!cursor.isAfterLast()) {
                    return parsePersistentObject(cursor);
                }
                return null;
            }
        });

    }

    private List<T> getAllWithQuery(String query, String[] whereArgs) {
        return executeQuery(query, whereArgs, new ResultHandler<List<T>>() {
            @Override
            public List<T> handle(Cursor cursor) throws SQLException {
                List<T> listOfPersistentObjects = new ArrayList<>();
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    T persistentObject = parsePersistentObject(cursor);
                    listOfPersistentObjects.add(persistentObject);
                    cursor.moveToNext();
                }

                return listOfPersistentObjects;
            }
        });
    }

    private boolean isInDb(String tableName, String column, String columnValue) {
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


    @Override
    public final void insertOrUpdate(T persistentObject) {
        insertOrUpdate(getDbName(), getContentValues(persistentObject), getDefaultPrimaryColumn(), getDefaultPrimaryValue(persistentObject));
    }

    @Override
    public final boolean isInDb(T persistentObject) {
        return isInDb(getDbName(), getDefaultPrimaryColumn(), getDefaultPrimaryValue(persistentObject));
    }
}
