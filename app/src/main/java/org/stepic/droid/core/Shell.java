package org.stepic.droid.core;

import android.content.Context;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.store.operations.DbOperationsSections;
import org.stepic.droid.util.SharedPreferenceHelper;
import org.stepic.droid.web.IApi;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Shell implements IShell {

    Context mContext;

    @Inject
    public Shell(Context context) {
        mContext = context;
        MainApplication.component(mContext).inject(this);
    }

    @Inject
    IScreenManager mScreenProvider;

    @Inject
    IApi mApi;

    @Inject
    SharedPreferenceHelper mSharedPreferenceHelper;

    @Override
    public IScreenManager getScreenProvider() {
        return mScreenProvider;
    }

    @Override
    public IApi getApi() {
        return mApi;
    }

    @Override
    public SharedPreferenceHelper getSharedPreferenceHelper() {
        return mSharedPreferenceHelper;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public DbOperationsCourses getDbOperationsCourses(DbOperationsCourses.Table type) {
        return new DbOperationsCourses(mContext, type); //how much objects here?
    }

    @Override
    public DbOperationsSections getDbOperationsSection() {
        return new DbOperationsSections(mContext);
    }

}
