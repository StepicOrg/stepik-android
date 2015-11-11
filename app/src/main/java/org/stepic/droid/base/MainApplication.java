package org.stepic.droid.base;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.core.DaggerStepicCoreComponent;
import org.stepic.droid.core.StepicCoreComponent;
import org.stepic.droid.core.StepicDefaultModule;

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

        // Инициализация AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), "fd479031-bdf4-419e-8d8f-6895aab23502");
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(this);
    }

    public static StepicCoreComponent component(Context context) {
        return ((MainApplication) context.getApplicationContext()).component;
    }


    public static StepicCoreComponent component() {
        return ((MainApplication) getAppContext()).component;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getAppContext() {
        return application.getApplicationContext();
    }

}
