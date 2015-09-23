package org.stepic.droid.store.operations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.operations.IDatabaseOperations;
import org.stepic.droid.store.structure.DBStructureBase;
import org.stepic.droid.store.structure.DBStructureCourses;

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

    public void dropDatabase () {
        MainApplication.getAppContext().deleteDatabase(DBStructureBase.FILE_NAME);
    }



}
