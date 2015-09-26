package org.stepic.droid.core;

import android.content.Context;

import org.stepic.droid.configuration.ConfigRelease;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.util.SharedPreferenceHelper;
import org.stepic.droid.web.Api;
import org.stepic.droid.web.HttpManager;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.IHttpManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class StepicDefaultModule {

    private final Context mContext;

    public StepicDefaultModule(Context context) {
        this.mContext = context;
    }

    @Provides @Singleton
    public IScreenManager provideIScreenManager() {
        return new ScreenManager();
    }

    @Provides @Singleton
    public IShell provideIShell(Context context) {
        return new Shell(context);
    }

    @Provides @Singleton
    public IConfig provideIConfig(Context context) {
        return new ConfigRelease(context);
    }


    @Provides @Singleton
    public IApi provideIApi(Context context) {
        return new Api(context);
    }

    @Provides @Singleton
    public IHttpManager provideIHttpManager(Context context) {
        return new HttpManager(context);
    }

    @Provides @Singleton
    public SharedPreferenceHelper provideSharedPreferencesHelper () {return  new SharedPreferenceHelper();}

    @Provides @Singleton  public Context provideApplicationContext() {
        return mContext;
    }
//    @Provides @Singleton  public DbOperationsCourses provideDbOperationsCourses(Context context) {
//        return new DbOperationsCourses(context);
//    }
}
