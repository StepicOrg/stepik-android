package org.stepic.droid.storage.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.storage.operations.DatabaseOperations;
import org.stepic.droid.storage.operations.ResultHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class DaoBase<T> implements IDao<T> {


    private final DatabaseOperations databaseOperations;

    public DaoBase(@NotNull DatabaseOperations databaseOperations) {
        this.databaseOperations = databaseOperations;
    }


    private void insertOrUpdate(String tableName, ContentValues cv, String primaryKeyColumn, String primaryValue) {
        if (isInDb(primaryKeyColumn, primaryValue)) {
            String whereClause = primaryKeyColumn + "=?";
            String[] whereArgs = new String[]{primaryValue};
            databaseOperations.executeUpdate(tableName, cv, whereClause, whereArgs);
        } else {
            databaseOperations.executeInsert(tableName, cv);
        }
    }

    @NotNull
    @Override
    public final List<T> getAll() {
        String query = "Select * from " + getDbName();
        return getAllWithQuery(query, null);
    }

    @Override
    @NotNull
    public final List<T> getAll(@NonNull String whereColumn, @NonNull String whereValue) {
        String query = "Select * from " + getDbName() + " where " + whereColumn + " = ?";
        return getAllWithQuery(query, new String[]{whereValue});
    }

    @Override
    @Nullable
    public T get(@NonNull String whereColumnName, @NonNull String whereValue) {
        String query = "Select * from " + getDbName() + " where " + whereColumnName + " = ?";
        return databaseOperations.executeQuery(query, new String[]{whereValue}, new ResultHandler<T>() {
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

    @Override
    public final void update(@NonNull String whereColumn, @NonNull String whereValue, @NonNull ContentValues contentValues) {
        databaseOperations.executeUpdate(getDbName(), contentValues, whereColumn + "=?", new String[]{whereValue});
    }

    @Override
    public void remove(@NonNull String whereColumn, @NonNull String whereValue) {
        String whereClause = whereColumn + " =?";
        databaseOperations.executeDelete(getDbName(), whereClause, new String[]{whereValue});
    }

    @NotNull
    @Override
    public final List<T> getAllInRange(@NonNull String whereColumn, @NonNull String commaSeparatedIds) {
        String query = "Select * from " + getDbName() + " where " + whereColumn + " IN (" + commaSeparatedIds + ")";
        return getAllWithQuery(query, null);
    }

    protected List<T> getAllWithQuery(String query, @Nullable String[] whereArgs) {
        return databaseOperations.executeQuery(query, whereArgs, new ResultHandler<List<T>>() {
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

    @Override
    public final boolean isInDb(@NotNull String column, @NotNull String columnValue) {
        String Query = "Select * from " + getDbName() + " where " + column + " = ?";
        return databaseOperations.executeQuery(Query, new String[]{columnValue}, new ResultHandler<Boolean>() {
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
    public void insertOrReplace(T persistentObject) {
        databaseOperations.executeReplace(getDbName(), getContentValues(persistentObject));
    }

    @Override
    public void insertOrUpdate(T persistentObject) {
        insertOrUpdate(getDbName(), getContentValues(persistentObject), getDefaultPrimaryColumn(), getDefaultPrimaryValue(persistentObject));
    }

    @Override
    public final boolean isInDb(T persistentObject) {
        return isInDb(getDefaultPrimaryColumn(), getDefaultPrimaryValue(persistentObject));
    }

    abstract String getDbName();

    abstract String getDefaultPrimaryColumn();

    abstract String getDefaultPrimaryValue(T persistentObject);

    abstract ContentValues getContentValues(T persistentObject);

    abstract T parsePersistentObject(Cursor cursor);

    @Override
    public void removeAll() {
        databaseOperations.executeDelete(getDbName(), null, null);
    }
}
