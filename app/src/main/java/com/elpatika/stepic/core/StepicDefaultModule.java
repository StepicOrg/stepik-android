package com.elpatika.stepic.core;

import android.content.Context;

import com.elpatika.stepic.configuration.ConfigRelease;
import com.elpatika.stepic.configuration.IConfig;
import com.elpatika.stepic.util.SharedPreferenceHelper;
import com.elpatika.stepic.web.Api;
import com.elpatika.stepic.web.HttpManager;
import com.elpatika.stepic.web.IApi;
import com.elpatika.stepic.web.IHttpManager;
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
}
