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
}
