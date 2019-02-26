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
import java.util.Map;

import kotlin.collections.CollectionsKt;

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

    @NotNull
    @Override
    public List<T> getAll(@NotNull Map<String, String> whereArgs) {
        final String query = "SELECT * FROM " + getDbName() + " WHERE ";
        final String where = CollectionsKt.joinToString(whereArgs.keySet(), " = ? AND ", "", "", -1, "" , null) + " = ?";
        return getAllWithQuery(query + where, whereArgs.values().toArray(new String[]{}));
    }

    @Override
    @Nullable
    public T get(@NonNull String whereColumnName, @NonNull String whereValue) {
        String query = "Select * from " + getDbName() + " where " + whereColumnName + " = ?";
        T persistentObject = databaseOperations.executeQuery(query, new String[]{whereValue}, new ResultHandler<T>() {
            @Override
            public T handle(Cursor cursor) throws SQLException {
                cursor.moveToFirst();

                if (!cursor.isAfterLast()) {
                    return parsePersistentObject(cursor);
                }
                return null;
            }
        });
        if (persistentObject != null) {
            persistentObject = populateNestedObjects(persistentObject);
        }
        return persistentObject;
    }

    @Override
    @Nullable
    public T get(@NotNull Map<String, String> whereArgs) {
        final String selector = "SELECT * FROM " + getDbName() + " WHERE ";
        final String where = CollectionsKt.joinToString(whereArgs.keySet(), " = ? AND ", "", "", -1, "" , null) + " = ?";
        final String query = selector + where + " LIMIT 1";

        T persistentObject =  databaseOperations.executeQuery(query, whereArgs.values().toArray(new String[]{}), new ResultHandler<T>() {
            @Override
            public T handle(Cursor cursor) throws SQLException {
                if (cursor.moveToNext()) {
                    return parsePersistentObject(cursor);
                } else {
                    return null;
                }
            }
        });
        if (persistentObject != null) {
            persistentObject = populateNestedObjects(persistentObject);
        }
        return persistentObject;
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

    @Override
    public void remove(@NotNull Map<String, String> whereArgs) {
        final String where = CollectionsKt.joinToString(whereArgs.keySet(), " = ? AND ", "", "", -1, "" , null) + " = ?";
        databaseOperations.executeDelete(getDbName(), where, whereArgs.values().toArray(new String[]{}));
    }

    @Override
    public void removeAllInRange(@NotNull String whereColumn, @NotNull String commaSeparatedIds) {
        String query = whereColumn + " IN (" + commaSeparatedIds + ")";
        databaseOperations.executeDelete(getDbName(), query, null);
    }

    @NotNull
    @Override
    public final List<T> getAllInRange(@NonNull String whereColumn, @NonNull String commaSeparatedIds) {
        String query = "Select * from " + getDbName() + " where " + whereColumn + " IN (" + commaSeparatedIds + ")";
        return getAllWithQuery(query, null);
    }

    @NotNull
    @Override
    public List<T> getAllWithQuery(@NotNull String query, @Nullable String[] whereArgs) {
        List<T> objects = databaseOperations.executeQuery(query, whereArgs, new ResultHandler<List<T>>() {
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
        for (int i = 0; i < objects.size(); i++) {
            objects.set(i, populateNestedObjects(objects.get(i)));
        }
        return objects;
    }

    protected <U> U rawQuery(String query, @Nullable String[] whereArgs, ResultHandler<U> resultHandler) {
        return databaseOperations.executeQuery(query, whereArgs, resultHandler);
    }

    protected void executeSql(@NotNull String query, @Nullable Object[] args) {
        databaseOperations.executeSql(query, args);
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
        storeNestedObjects(persistentObject);
    }

    @Override
    public void insertOrReplaceAll(@NotNull List<? extends T> persistentObjects) {
        List<ContentValues> values = new ArrayList<>(persistentObjects.size());
        for (T object: persistentObjects) {
            values.add(getContentValues(object));
        }
        databaseOperations.executeReplaceAll(getDbName(), values);
        for (T object: persistentObjects) {
            storeNestedObjects(object);
        }
    }

    @Override
    public void insertOrUpdate(T persistentObject) {
        insertOrUpdate(getDbName(), getContentValues(persistentObject), getDefaultPrimaryColumn(), getDefaultPrimaryValue(persistentObject));
        storeNestedObjects(persistentObject);
    }

    @Override
    public final boolean isInDb(T persistentObject) {
        return isInDb(getDefaultPrimaryColumn(), getDefaultPrimaryValue(persistentObject));
    }

    protected abstract String getDbName();

    protected abstract String getDefaultPrimaryColumn();

    protected abstract String getDefaultPrimaryValue(T persistentObject);

    protected abstract ContentValues getContentValues(T persistentObject);

    protected abstract T parsePersistentObject(Cursor cursor);

    protected T populateNestedObjects(T persistentObject) { return persistentObject; }
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected void storeNestedObjects(T persistentObject) {}

    @Override
    public void removeAll() {
        databaseOperations.executeDelete(getDbName(), null, null);
    }
}
