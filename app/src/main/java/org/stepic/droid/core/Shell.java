package org.stepic.droid.core;

import android.content.Context;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.IApi;

import javax.inject.Inject;
import javax.inject.Singleton;

@Deprecated
@Singleton
public class Shell implements IShell {

    Context context;

    @Inject
    public Shell(Context context) {
        this.context = context;
        MainApplication.component(this.context).inject(this);
    }

    @Inject
    IScreenManager screenProvider;

    @Inject
    IApi api;

    @Inject
    SharedPreferenceHelper sharedPreferenceHelper;

    @Override
    public IScreenManager getScreenProvider() {
        return screenProvider;
    }

    @Override
    public IApi getApi() {
        return api;
    }

    @Override
    public SharedPreferenceHelper getSharedPreferenceHelper() {
        return sharedPreferenceHelper;
    }
}
