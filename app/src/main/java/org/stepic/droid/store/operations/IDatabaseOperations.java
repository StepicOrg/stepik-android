package org.stepic.droid.store.operations;

import android.database.Cursor;

import java.sql.SQLException;

public interface IDatabaseOperations {
    void open() throws SQLException;

    void close();

    Cursor getCursor();
}
