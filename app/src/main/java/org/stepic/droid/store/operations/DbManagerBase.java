package org.stepic.droid.store.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
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
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Illegal state: working with database on UI thread");
        }
        Log.i("database", "open " + Thread.currentThread().getName());
        database = dbHelper.getWritableDatabase();
    }

    protected synchronized void close() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Illegal state: working with database on UI thread");
        }
        Log.i("database", "close " + Thread.currentThread().getName());
        database.close();
    }

    @Override
    public void dropDatabase() {
        MainApplication.getAppContext().deleteDatabase(DBStructureBase.FILE_NAME);
    }
}
