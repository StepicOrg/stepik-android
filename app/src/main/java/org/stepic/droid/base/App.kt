package org.stepic.droid.base

import android.content.Context
import android.os.Build
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.android.billingclient.api.BillingClient
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.vk.api.sdk.VK
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import io.branch.referral.Branch
import org.stepic.droid.BuildConfig
import org.stepic.droid.R
import org.stepic.droid.analytic.experiments.SplitTestsHolder
import org.stepic.droid.code.highlight.ParserContainer
import org.stepic.droid.core.ComponentManager
import org.stepic.droid.core.ComponentManagerImpl
import org.stepic.droid.di.AppCoreComponent
import org.stepic.droid.di.DaggerAppCoreComponent
import org.stepic.droid.di.storage.DaggerStorageComponent
import org.stepic.droid.persistence.downloads.DownloadsSyncronizer
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DebugToolsHelper
import org.stepic.droid.util.NotificationChannelInitializer
import org.stepik.android.domain.view_assignment.service.DeferrableViewAssignmentReportServiceContainer
import org.stepik.android.view.injection.billing.DaggerBillingComponent
import ru.nobird.android.view.base.ui.extension.isMainProcess
import timber.log.Timber
import javax.inject.Inject
import javax.net.ssl.SSLContext

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
    internal lateinit var downloadsSyncronizer: DownloadsSyncronizer

    /**
     * Init split tests on app start
     */
    @Inject
    internal lateinit var splitTestsHolder: SplitTestsHolder

    /**
     * Init step view publisher service on startup
     */
    @Inject
    internal lateinit var stepDeferrableViewReportService: DeferrableViewAssignmentReportServiceContainer

    //don't use this field, it is just for init ASAP in background thread
    @Inject
    internal lateinit var codeParserContainer: ParserContainer

    @Inject
    internal lateinit var billingClient: BillingClient

    @Inject
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    override fun onCreate() {
        super.onCreate()
        if (!isMainProcess) return

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        refWatcher = LeakCanary.install(this)

        setTheme(R.style.AppTheme)

        init()
    }

    private fun init() {
        application = this

        DebugToolsHelper.initDebugTools(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WebView.setDataDirectorySuffix("web")
            }
            WebView.setWebContentsDebuggingEnabled(true)
        }

//        AppEventsLogger.activateApp(this)
        VK.initialize(this)
        
        // init AppMetrica SDK
        YandexMetrica.activate(applicationContext, YandexMetricaConfig.newConfigBuilder("fd479031-bdf4-419e-8d8f-6895aab23502").build())
        YandexMetrica.enableActivityAutoTracking(this)

        component = DaggerAppCoreComponent.builder()
                .context(application)
                .setStorageComponent(DaggerStorageComponent
                        .builder()
                        .context(application)
                        .build())
                .setBillingComponent(DaggerBillingComponent
                    .builder()
                    .context(application)
                    .build())
                .build()

        component.inject(this)


        componentManager = ComponentManagerImpl(component)

        Branch.getAutoInstance(this)
        initChannels()
        initNightMode()
    }

    private fun initChannels() {
        NotificationChannelInitializer.initNotificationChannels(this)
    }

    private fun initNightMode() {
        AppCompatDelegate.setDefaultNightMode(sharedPreferenceHelper.nightMode)
    }
}
