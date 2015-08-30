package com.elpatika.stepic.core;

import com.elpatika.stepic.web.IApi;
import com.google.inject.Inject;
import com.google.inject.Singleton;



@Singleton
public class Shell implements IShell {

    @Inject
    private IScreenManager mScreenProvider;

    @Inject
    private IApi mApi;

    @Override
    public IScreenManager getScreenProvider() {
        return mScreenProvider;
    }

    @Override
    public IApi getApi() {
        return mApi;
    }
}
