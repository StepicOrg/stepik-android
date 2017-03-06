package org.stepic.droid.core;

import android.content.Context;

import org.stepic.droid.base.App;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.Api;

import javax.inject.Inject;
import javax.inject.Singleton;

@Deprecated
@Singleton
public class ShellImpl implements Shell {

    Context context;

    @Inject
    public ShellImpl(Context context) {
        this.context = context;
        App.component(this.context).inject(this);
    }

    @Inject
    ScreenManager screenProvider;

    @Inject
    Api api;

    @Inject
    SharedPreferenceHelper sharedPreferenceHelper;

    @Override
    public ScreenManager getScreenProvider() {
        return screenProvider;
    }

    @Override
    public Api getApi() {
        return api;
    }

    @Override
    public SharedPreferenceHelper getSharedPreferenceHelper() {
        return sharedPreferenceHelper;
    }
}
