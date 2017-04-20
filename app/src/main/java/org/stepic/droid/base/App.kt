package org.stepic.droid.base

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.stetho.Stetho
import com.vk.sdk.VKSdk
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.BuildConfig
import org.stepic.droid.R
import org.stepic.droid.core.ComponentManager
import org.stepic.droid.core.ComponentManagerImpl
import org.stepic.droid.di.AppCoreComponent
import org.stepic.droid.di.DaggerAppCoreComponent
import org.stepic.droid.di.storage.DaggerStorageComponent
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.storage.InitialDownloadUpdater
import timber.log.Timber
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import javax.inject.Inject

class App : MultiDexApplication() {

    companion object {
        lateinit var application: App

        fun component(): AppCoreComponent {
            return application.component
        }

        fun getAppContext(): Context {
            return application.applicationContext
        }

        fun getComponentManager(): ComponentManager {
            return application.componentManager
        }
    }

    private lateinit var component: AppCoreComponent
    private lateinit var componentManager: ComponentManager

    @Inject
    lateinit var downloadUpdater: InitialDownloadUpdater

    @Inject
    lateinit var fontsProvider: FontsProvider

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        application = this
        Stetho.initializeWithDefaults(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        VKSdk.initialize(this)

        component = DaggerAppCoreComponent.builder()
                .context(application)
                .setStorageComponent(DaggerStorageComponent
                        .builder()
                        .context(application)
                        .build())
                .build()

        component.inject(this)


        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath(fontsProvider.provideFontPath(FontType.regular))
                .setFontAttrId(R.attr.fontPath)
                .build()
        )

        componentManager = ComponentManagerImpl(component)

        downloadUpdater.onCreateApp()

        // init AppMetrica SDK
        YandexMetrica.activate(applicationContext, "fd479031-bdf4-419e-8d8f-6895aab23502")
        YandexMetrica.enableActivityAutoTracking(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
