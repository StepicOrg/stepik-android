package org.stepic.droid.core;


import android.content.Context;

import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.store.operations.DbOperationsSections;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.IApi;

public interface IShell {

    IScreenManager getScreenProvider();
    IApi getApi();
    SharedPreferenceHelper getSharedPreferenceHelper();
    Context getContext();
    DbOperationsCourses getDbOperationsCourses(DbOperationsCourses.Table type );
    DbOperationsSections getDbOperationsSection();
}
