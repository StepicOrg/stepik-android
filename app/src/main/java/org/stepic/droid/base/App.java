package org.stepic.droid.base;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.stetho.Stetho;
import com.vk.sdk.VKSdk;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.BuildConfig;
import org.stepic.droid.R;
import org.stepic.droid.core.ComponentManager;
import org.stepic.droid.core.ComponentManagerImpl;
import org.stepic.droid.core.components.AppCoreComponent;
import org.stepic.droid.core.components.DaggerAppCoreComponent;
import org.stepic.droid.fonts.FontType;
import org.stepic.droid.fonts.FontsProvider;
import org.stepic.droid.store.InitialDownloadUpdater;

import javax.inject.Inject;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends MultiDexApplication {

    protected static App application;
    private AppCoreComponent component;
    private ComponentManager componentManager;

    //    private RefWatcher refWatcher;

    @Inject
    InitialDownloadUpdater downloadUpdater;

    @Inject
    FontsProvider fontsProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }


    private void init() {
//        refWatcher = LeakCanary.install(this);
        application = this;
        Stetho.initializeWithDefaults(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        VKSdk.initialize(this);

        component = DaggerAppCoreComponent.builder()
                .context(application)
                .build();

        component.inject(this);


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(fontsProvider.provideFontPath(FontType.regular))
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        componentManager = new ComponentManagerImpl(component);

        downloadUpdater.onCreateApp();

        // init AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), "fd479031-bdf4-419e-8d8f-6895aab23502");
        YandexMetrica.enableActivityAutoTracking(this);
    }
//    public static RefWatcher getRefWatcher(Context context) {
//        App application = (App) context.getApplicationContext();
//        return application.refWatcher;
//    }

    public static AppCoreComponent component() {
        return application.component;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getAppContext() {
        return application.getApplicationContext();
    }

    public static ComponentManager getComponentManager() {
        return application.componentManager;
    }

}
