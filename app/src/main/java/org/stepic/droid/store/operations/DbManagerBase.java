package org.stepic.droid.store.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.structure.DBStructureBase;

public abstract class DbManagerBase implements IDatabaseManager {

    protected SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    protected Context mContext;


    public DbManagerBase(Context context) {
        mContext = context;
        dbHelper = new DatabaseHelper(context);
    }

    protected synchronized void open() {
        Log.i("database", "open " + Thread.currentThread().getName());
        database = dbHelper.getWritableDatabase();
    }

    protected synchronized void close() {
        Log.i("database", "close " + Thread.currentThread().getName());
        database.close();
    }

    @Override
    public void dropDatabase() {
        MainApplication.getAppContext().deleteDatabase(DBStructureBase.FILE_NAME);
    }
}
