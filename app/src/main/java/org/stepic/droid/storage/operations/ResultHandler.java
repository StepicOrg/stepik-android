package org.stepic.droid.storage.operations;

import android.database.Cursor;
import android.database.SQLException;

public interface ResultHandler<T> {
    T handle(Cursor cursor) throws SQLException;
}
