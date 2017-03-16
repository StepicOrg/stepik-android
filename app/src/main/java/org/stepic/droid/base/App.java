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
import org.stepic.droid.core.components.DaggerStorageComponent;
import org.stepic.droid.core.components.StorageComponent;
import org.stepic.droid.core.modules.AppCoreModule;
import org.stepic.droid.core.modules.StorageModule;
import org.stepic.droid.store.InitialDownloadUpdater;

import javax.inject.Inject;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends MultiDexApplication {

    protected static App application;
    private AppCoreComponent component;
    private StorageComponent storageComponent;
    private ComponentManager componentManager;

    //    private RefWatcher refWatcher;

    @Inject
    InitialDownloadUpdater downloadUpdater;

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

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NotoSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        VKSdk.initialize(this);

        StorageModule storageModule = new StorageModule(this);
        storageComponent = DaggerStorageComponent.builder().
                storageModule(storageModule).build();

        component = DaggerAppCoreComponent.builder()
                .appCoreModule(new AppCoreModule(application))
                .storageModule(storageModule)
                .build();

        component.inject(this);

        componentManager = new ComponentManagerImpl(component);

        downloadUpdater.onCreateApp();

        // Инициализация AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), "fd479031-bdf4-419e-8d8f-6895aab23502");
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(this);
    }
//    public static RefWatcher getRefWatcher(Context context) {
//        App application = (App) context.getApplicationContext();
//        return application.refWatcher;
//    }

    public static AppCoreComponent component(Context context) {
        return ((App) context.getApplicationContext()).component;
    }

    public static AppCoreComponent component() {
        return application.component;
    }

    public static StorageComponent storageComponent() {
        return application.storageComponent;
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
