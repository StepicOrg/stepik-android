package org.stepic.droid.base;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.core.DaggerStepicCoreComponent;
import org.stepic.droid.core.StepicCoreComponent;
import org.stepic.droid.core.StepicDefaultModule;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MainApplication extends MultiDexApplication {

    protected static MainApplication application;
    private StepicCoreComponent component;

//    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
//        refWatcher = LeakCanary.install(this);
        application = this;
        Fresco.initialize(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/NotoSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        component = DaggerStepicCoreComponent.builder().
                stepicDefaultModule(new StepicDefaultModule(application)).build();

        // Инициализация AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), "fd479031-bdf4-419e-8d8f-6895aab23502");
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(this);
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        MainApplication application = (MainApplication) context.getApplicationContext();
//        return application.refWatcher;
//    }

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
