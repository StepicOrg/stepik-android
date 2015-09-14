package com.elpatika.stepic.base;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.elpatika.stepic.core.DaggerStepicCoreComponent;
import com.elpatika.stepic.core.StepicCoreComponent;
import com.elpatika.stepic.core.StepicDefaultModule;

public class MainApplication extends MultiDexApplication {

    protected static MainApplication application;
    private StepicCoreComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        application = this;

        component = DaggerStepicCoreComponent.builder().
                stepicDefaultModule(new StepicDefaultModule(application)).build();
    }

    public static StepicCoreComponent component(Context context) {
        return ((MainApplication) context.getApplicationContext()).component;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
