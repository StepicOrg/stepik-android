package com.elpatika.stepic.base;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.elpatika.stepic.core.StepicDefaultModule;
import com.google.inject.Injector;
import com.google.inject.Module;

import roboguice.RoboGuice;

/**
 * Created by kirillmakarov on 23.08.15.
 */
public class MainApplication extends MultiDexApplication {

    protected static MainApplication application;
    Injector injector;

    public static final MainApplication instance() {
        return application;
    }

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * Initializes the request manager, image cache,
     * all third party integrations and shared components.
     */
    private void init() {

        application = this;

        injector = RoboGuice.getOrCreateBaseApplicationInjector((Application) this, RoboGuice.DEFAULT_STAGE,
                (Module) RoboGuice.newDefaultRoboModule(this), (Module) new StepicDefaultModule());
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
