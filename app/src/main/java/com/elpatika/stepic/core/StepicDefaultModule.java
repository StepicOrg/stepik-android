package com.elpatika.stepic.core;

import com.elpatika.stepic.configuration.ConfigRelease;
import com.elpatika.stepic.configuration.IConfig;
import com.elpatika.stepic.web.Api;
import com.elpatika.stepic.web.HttpManager;
import com.elpatika.stepic.web.IApi;
import com.elpatika.stepic.web.IHttpManager;
import com.google.inject.AbstractModule;


public class StepicDefaultModule extends AbstractModule {

    @Override
    public void configure() {
        bind(IScreenManager.class).to(ScreenManager.class);
        bind(IShell.class).to(Shell.class);
        bind(IConfig.class).to(ConfigRelease.class);
        bind(IApi.class).to(Api.class);
        bind(IHttpManager.class).to(HttpManager.class);
    }
}
