package com.elpatika.stepic.core;

import com.elpatika.stepic.util.SharedPreferenceHelper;
import com.elpatika.stepic.web.IApi;
import com.google.inject.Inject;
import com.google.inject.Singleton;



@Singleton
public class Shell implements IShell {

    @Inject
    private IScreenManager mScreenProvider;

    @Inject
    private IApi mApi;

    @Inject
    private SharedPreferenceHelper mSharedPreferenceHelper;

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
