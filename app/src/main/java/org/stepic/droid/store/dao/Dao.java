package org.stepic.droid.store.dao;

public interface Dao<T> {
    void insertOrUpdate(T persistentObject);

    boolean isInDb(T persistentObject);
}
