package com.elpatika.stepic.core;

import android.content.Context;

import com.elpatika.stepic.base.MainApplication;
import com.elpatika.stepic.util.SharedPreferenceHelper;
import com.elpatika.stepic.web.IApi;

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
}
