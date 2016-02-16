package org.stepic.droid.store.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.structure.DBStructureBase;
import org.stepic.droid.util.RWLocks;

public abstract class DbManagerBase implements IDatabaseManager {

    protected SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    protected Context mContext;


    public DbManagerBase(Context context) {
        mContext = context;
        dbHelper = new DatabaseHelper(context);
    }

    protected void open() {
        RWLocks.DatabaseLock.writeLock().lock();
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            throw new IllegalStateException("Illegal state: working with database on UI thread");
//            it is ok
//        }
//        Log.i("database", "open " + Thread.currentThread().getName());
        database = dbHelper.getWritableDatabase();
    }

    protected void close() {
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            throw new IllegalStateException("Illegal state: working with database on UI thread");
        //it is ok.
//        }
//        Log.i("database", "close " + Thread.currentThread().getName());
        database.close();
        RWLocks.DatabaseLock.writeLock().unlock();
    }

    @Override
    public void dropDatabase() {
        MainApplication.getAppContext().deleteDatabase(DBStructureBase.FILE_NAME);
    }

    protected <T> T executeQuery(String sqlQuery, String[] selectionArgs, ResultHandler<T> handler) {
        try {
            open();
            Cursor cursor = database.rawQuery(sqlQuery, selectionArgs);
            T result = handler.handle(cursor);
            cursor.close();
            return result;
        } finally {
            close();
        }

    }

    protected void executeUpdate(String table, ContentValues values, String whereClause, String[] whereArgs) {
        try {
            open();
            database.update(table, values, whereClause, whereArgs);
        } finally {
            close();
        }
    }


}
