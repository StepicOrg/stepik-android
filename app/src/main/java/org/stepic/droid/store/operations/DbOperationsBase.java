package org.stepic.droid.store.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.structure.DBStructureBase;

import java.sql.SQLException;

public abstract class DbOperationsBase implements IDatabaseOperations {

    protected SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public DbOperationsBase(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    @Override
    public synchronized void  open() throws SQLException {
        Log.i("database", "open " + Thread.currentThread().getName());
        database = dbHelper.getWritableDatabase();
    }

    @Override
    public synchronized void close() {

        Log.i("database", "close " + Thread.currentThread().getName());
        database.close();
    }

    public void dropDatabase () {
        MainApplication.getAppContext().deleteDatabase(DBStructureBase.FILE_NAME);
    }

    public abstract void clearCache();

}
