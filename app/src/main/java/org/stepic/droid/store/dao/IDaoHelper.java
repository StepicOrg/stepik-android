package org.stepic.droid.store.dao;

import android.content.ContentValues;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IDaoHelper {
    boolean isInDb(String tableName, String column, String columnValue);

    void insertOrUpdate(String tableName, ContentValues cv, String primaryKeyColumn, String primaryValue);

    @NotNull
    <T> List<T> getAll(IDao<T> dao, String dbName);

    @NotNull
    <T> List<T> getAll(final IDao<T> dao, String tableName, String whereColumn, String whereValue);

    @Nullable
    <T> T get(final IDao<T> dao, String tableName, String whereColumn, String whereValue);
}
