package org.stepic.droid.core;


import android.content.Context;

import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.operations.DbOperationsBase;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.util.SharedPreferenceHelper;
import org.stepic.droid.web.IApi;

public interface IShell {

    IScreenManager getScreenProvider();
    IApi getApi();
    SharedPreferenceHelper getSharedPreferenceHelper();
    Context getContext();
    DbOperationsCourses getDbOperationsCourses(DbOperationsCourses.Table type );
}
