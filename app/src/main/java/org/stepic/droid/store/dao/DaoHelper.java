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

public final class DaoHelper implements IDaoHelper {
    protected SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;

    public DaoHelper(SQLiteOpenHelper openHelper) {
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

    @Override
    public void insertOrUpdate(String tableName, ContentValues cv, String primaryKeyColumn, String primaryValue) {
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
    public <T> List<T> getAll(final IDao<T> dao, String tableName) {
        String query = "Select * from " + tableName;
        return getAllWithQuery(dao, query, null);
    }

    @NotNull
    public <T> List<T> getAll(final IDao<T> dao, String tableName, String whereColumn, String whereValue) {
        String query = "Select * from " + tableName + " where " + whereColumn + " = ?";
        return getAllWithQuery(dao, query, new String[]{whereValue});
    }

    @Nullable
    @Override
    public <T> T get(final IDao<T> dao, String tableName, String whereColumn, String whereValue) {
        String query = "Select * from " + tableName + " where " + whereColumn + " = ?";
        return executeQuery(query, new String[]{whereValue}, new ResultHandler<T>() {
            @Override
            public T handle(Cursor cursor) throws SQLException {
                cursor.moveToFirst();

                if (!cursor.isAfterLast()) {
                    return dao.parsePersistentObject(cursor);
                }
                return null;
            }
        });

    }

    private <T> List<T> getAllWithQuery(final IDao<T> dao, String query, String[] whereArgs) {
        return executeQuery(query, null, new ResultHandler<List<T>>() {
            @Override
            public List<T> handle(Cursor cursor) throws SQLException {
                List<T> listOfPersistentObjects = new ArrayList<>();
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    T persistentObject = dao.parsePersistentObject(cursor);
                    listOfPersistentObjects.add(persistentObject);
                    cursor.moveToNext();
                }

                return listOfPersistentObjects;
            }
        });
    }


    @Override
    public boolean isInDb(String tableName, String column, String columnValue) {
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
