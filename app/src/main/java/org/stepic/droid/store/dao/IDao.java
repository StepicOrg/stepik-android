package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IDao<T> {
    void insertOrUpdate(T persistentObject);

    boolean isInDb(T persistentObject);

    boolean isInDb(String whereColumn, String value);

    @NotNull
    List<T> getAll();

    @NotNull
    List<T> getAll(String whereColumnName, String whereValue);

    T get(String whereColumnName, String whereValue);

    T parsePersistentObject(Cursor cursor);

    String getDbName();

    ContentValues getContentValues(T persistentObject);

    String getDefaultPrimaryColumn();

    String getDefaultPrimaryValue(T persistentObject);

    void update(String whereColumn, String whereValue, ContentValues contentValues);

    void delete(String whereColumn, String whereValue);

    List<T>  getAllInRange(String  whereColumn, String commaSeparatedIds);
}
