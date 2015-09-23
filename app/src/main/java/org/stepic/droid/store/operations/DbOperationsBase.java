package org.stepic.droid.store.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.operations.IDatabaseOperations;

import java.sql.SQLException;

public abstract class DbOperationsBase implements IDatabaseOperations {

    protected SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public DbOperationsBase(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    @Override
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    @Override
    public void close() {
        database.close();
    }
}
