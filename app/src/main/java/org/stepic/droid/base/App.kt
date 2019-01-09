package org.stepic.droid.base

import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.support.multidex.MultiDexApplication
import android.webkit.WebView
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.vk.sdk.VKSdk
import com.yandex.metrica.YandexMetrica
import io.branch.referral.Branch
import org.stepic.droid.BuildConfig
import org.stepic.droid.R
import org.stepic.droid.code.highlight.ParserContainer
import org.stepic.droid.core.ComponentManager
import org.stepic.droid.core.ComponentManagerImpl
import org.stepic.droid.di.AppCoreComponent
import org.stepic.droid.di.DaggerAppCoreComponent
import org.stepic.droid.di.storage.DaggerStorageComponent
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.persistence.downloads.DownloadsSyncronizer
import org.stepic.droid.util.NotificationChannelInitializer
import org.stepic.droid.util.StethoHelper
import org.stepic.droid.util.isMainProcess
import timber.log.Timber
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import javax.inject.Inject

class App : MultiDexApplication() {

    companion object {
        lateinit var application: App

        lateinit var refWatcher: RefWatcher
            private set

        fun component(): AppCoreComponent =
                application.component

        fun getAppContext(): Context =
                application.applicationContext

        fun componentManager(): ComponentManager =
                application.componentManager
    }

    private lateinit var component: AppCoreComponent
    private lateinit var componentManager: ComponentManager

    @Inject
    lateinit var downloadsSyncronizer: DownloadsSyncronizer

    @Inject
    lateinit var fontsProvider: FontsProvider


    //don't use this field, it is just for init ASAP in background thread
    @Inject
    lateinit var codeParserContainer: ParserContainer

    override fun onCreate() {
        super.onCreate()
        if (!isMainProcess) return

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        refWatcher = LeakCanary.install(this)
        init()
    }

    private fun init() {
        application = this

        StethoHelper.initStetho(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    WebView.setDataDirectorySuffix("web")
                }
                WebView.setWebContentsDebuggingEnabled(true)
            }
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

        // init AppMetrica SDK
        YandexMetrica.activate(applicationContext, "fd479031-bdf4-419e-8d8f-6895aab23502")
        YandexMetrica.enableActivityAutoTracking(this)

        Branch.getAutoInstance(this)
        initChannels()
    }

    private fun initChannels() {
        NotificationChannelInitializer.initNotificationChannels(this)
    }
}
