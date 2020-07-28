package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main_feed.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.StepikAnalytic
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.core.StepikDevicePoster
import org.stepic.droid.core.earlystreak.contract.EarlyStreakListener
import org.stepic.droid.core.presenters.ProfileMainFeedPresenter
import org.stepic.droid.core.presenters.contracts.ProfileMainFeedView
import org.stepic.droid.notifications.badges.NotificationsBadgesListener
import org.stepic.droid.notifications.badges.NotificationsBadgesManager
import org.stepic.droid.ui.activities.contracts.RootScreen
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment
import org.stepic.droid.ui.fragments.HomeFragment
import org.stepic.droid.ui.fragments.NotificationsFragment
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.commit
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.streak.interactor.StreakInteractor
import org.stepik.android.model.Course
import org.stepik.android.view.catalog.ui.fragment.CatalogFragment
import org.stepik.android.view.profile.ui.fragment.ProfileFragment
import org.stepik.android.view.streak.ui.dialog.StreakNotificationDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject


class MainFeedActivity : BackToExitActivityWithSmartLockBase(),
        BottomNavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemReselectedListener,
        RootScreen,
        ProfileMainFeedView,
        EarlyStreakListener,
        NotificationsBadgesListener,
        TimeIntervalPickerDialogFragment.Companion.Callback {

    companion object {
        const val CURRENT_INDEX_KEY = "currentIndexKey"

        const val reminderKey = "reminderKey"
        const val defaultIndex: Int = 0
        private const val LOGGED_ACTION = "LOGGED_ACTION"

        private const val CATALOG_DEEPLINK = "catalog"
        private const val NOTIFICATIONS_DEEPLINK = "notifications"

        const val HOME_INDEX: Int = 1
        const val CATALOG_INDEX: Int = 2
        const val PROFILE_INDEX: Int = 3
        const val NOTIFICATIONS_INDEX: Int = 4

        fun launchAfterLogin(sourceActivity: Activity, course: Course?) {
            val intent = Intent(sourceActivity, MainFeedActivity::class.java)
            if (course != null) {
                intent.putExtra(AppConstants.KEY_COURSE_BUNDLE, course)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.action = LOGGED_ACTION
            sourceActivity.startActivity(intent)
        }
    }

    @Inject
    lateinit var stepikAnalytic: StepikAnalytic

    @Inject
    lateinit var profileMainFeedPresenter: ProfileMainFeedPresenter

    @Inject
    lateinit var stepikDevicePoster: StepikDevicePoster

    @Inject
    lateinit var earlyStreakClient: Client<EarlyStreakListener>

    @Inject
    lateinit var notificationsBadgesClient: Client<NotificationsBadgesListener>

    @Inject
    lateinit var streakPresenter: StreakInteractor

    @Inject
    lateinit var notificationsBadgesManager: NotificationsBadgesManager

    @Inject
    internal lateinit var threadPoolExecutor: ThreadPoolExecutor

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationClickedCheck(intent)

        openFragment(intent)
    }

    private fun notificationClickedCheck(intent: Intent) {
        val action = intent.action
        if (action != null) {
            if (action == AppConstants.OPEN_NOTIFICATION) {
                analytic.reportEvent(AppConstants.OPEN_NOTIFICATION)
            } else if (action == AppConstants.OPEN_NOTIFICATION_FOR_ENROLL_REMINDER) {
                var dayTypeString: String? = intent.getStringExtra(reminderKey)
                if (dayTypeString == null) {
                    dayTypeString = ""
                }
                analytic.reportEvent(Analytic.Notification.REMIND_OPEN, dayTypeString)
                Timber.d(Analytic.Notification.REMIND_OPEN)
                sharedPreferenceHelper.clickEnrollNotification(DateTimeHelper.nowUtc())
            } else if (action == AppConstants.OPEN_NOTIFICATION_FROM_STREAK) {
                sharedPreferenceHelper.resetNumberOfStreakNotifications()
                if (intent.hasExtra(Analytic.Streak.NOTIFICATION_TYPE_PARAM)) {
                    val notificationType = intent.getSerializableExtra(Analytic.Streak.NOTIFICATION_TYPE_PARAM) as Analytic.Streak.NotificationType
                    val bundle = Bundle()
                    bundle.putString(Analytic.Streak.NOTIFICATION_TYPE_PARAM, notificationType.name)
                    analytic.reportEvent(Analytic.Streak.STREAK_NOTIFICATION_OPENED, bundle)
                } else {
                    analytic.reportEvent(Analytic.Streak.STREAK_NOTIFICATION_OPENED)
                }
            } else if (action == AppConstants.OPEN_SHORTCUT_CATALOG) {
                analytic.reportEvent(Analytic.Shortcut.OPEN_CATALOG)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                    getSystemService(ShortcutManager::class.java)
                        ?.reportShortcutUsed(AppConstants.CATALOG_SHORTCUT_ID)
                }

            }

            //after tracking check on null user
            if (sharedPreferenceHelper.authResponseFromStore == null) {
                screenManager.openSplash(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager()
            .mainFeedComponent()
            .inject(this)

        setContentView(R.layout.activity_main_feed)

        notificationClickedCheck(intent)

        initGoogleApiClient(true)

        // TODO Testing analytics
//        analytic.report(TestAnalyticEvent(12345L, CourseViewSource.MyCourses))

//        stepikAnalytic.flushEvents()

        initNavigation()

        earlyStreakClient.subscribe(this)
        if (checkPlayServices() && !sharedPreferenceHelper.isGcmTokenOk) {
            threadPoolExecutor.execute {
                stepikDevicePoster.registerDevice()
            }
        }

        val course = courseFromExtra
        if (course != null) {
            intent.removeExtra(AppConstants.KEY_COURSE_BUNDLE)
            screenManager.showCourseDescription(this, course, CourseViewSource.Auth, true)
        }

        if (savedInstanceState == null) {
            openFragment(intent, forceHome = true)
        }

        profileMainFeedPresenter.attachView(this)
        notificationsBadgesClient.subscribe(this)
        notificationsBadgesManager.fetchAndThenSyncCounter()
        if (savedInstanceState == null) {
            profileMainFeedPresenter.fetchProfile()
        }

        onShowStreakSuggestion()
    }

    private fun getFragmentIndexFromIntent(intent: Intent?): Int {
        if (intent == null) return -1

        intent.data?.pathSegments?.let {
            when {
                it.contains(NOTIFICATIONS_DEEPLINK) -> return NOTIFICATIONS_INDEX
                it.contains(CATALOG_DEEPLINK)       -> return CATALOG_INDEX
                else -> {}
            }
        }

        return intent.getIntExtra(CURRENT_INDEX_KEY, -1)
    }

    private fun openFragment(launchIntent: Intent?, forceHome: Boolean = false) {
        if (forceHome) {
            setFragment(R.id.home)
        }

        when (getFragmentIndexFromIntent(launchIntent)) {
            CATALOG_INDEX       -> navigationView.selectedItemId = R.id.catalog
            PROFILE_INDEX       -> navigationView.selectedItemId = R.id.profile
            NOTIFICATIONS_INDEX -> navigationView.selectedItemId = R.id.notifications
            else -> {
                //do nothing
            }
        }
    }

    private fun initNavigation() {
        navigationView.setOnNavigationItemSelectedListener(::onNavigationItemSelected)
        navigationView.setOnNavigationItemReselectedListener(::onNavigationItemReselected)
    }

    private fun showCurrentFragment(@IdRes id: Int) {
        setFragment(id)
    }

    override fun onDestroy() {
        earlyStreakClient.unsubscribe(this)
        profileMainFeedPresenter.detachView(this)
        notificationsBadgesClient.unsubscribe(this)
        if (isFinishing) {
            App.componentManager().releaseMainFeedComponent()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (navigationView.selectedItemId == R.id.home) {
            finish()
            return
        } else {
            navigationView.selectedItemId = R.id.home
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        sendOpenUserAnalytic(menuItem.itemId)
        showCurrentFragment(menuItem.itemId)
        return true
    }

    override fun onNavigationItemReselected(menuItem: MenuItem) {
//        scrollUp()
    }

    private fun sendOpenUserAnalytic(itemId: Int) {
        when (itemId) {
            R.id.home -> analytic.reportEvent(Analytic.Screens.USER_OPEN_MY_COURSES)
            R.id.catalog -> {
                analytic.reportEvent(Analytic.Screens.USER_OPEN_CATALOG)
                if (sharedPreferenceHelper.authResponseFromStore == null) {
                    analytic.reportEvent(Analytic.Anonymous.BROWSE_COURSES_DRAWER)
                }
            }
            R.id.profile -> analytic.reportEvent(Analytic.Screens.USER_OPEN_PROFILE)
            R.id.notifications -> analytic.reportEvent(Analytic.Screens.USER_OPEN_NOTIFICATIONS)
        }
    }

    private fun setFragment(@IdRes id: Int) {
        val fragmentTag = getNextFragmentTag(id)

        supportFragmentManager.commit {
            supportFragmentManager.fragments.forEach { hide(it) }
            val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
            if (fragment != null) {
                show(fragment)
            } else {
                val nextFragment = getNextFragmentInstance(id)
                add(R.id.frame, nextFragment, nextFragment::class.java.simpleName)
            }
        }
    }

    private fun getNextFragmentTag(@IdRes menuId: Int): String =
        when (menuId) {
            R.id.home ->
                HomeFragment::class.java.simpleName

            R.id.catalog ->
                CatalogFragment::class.java.simpleName

            R.id.profile ->
                ProfileFragment::class.java.simpleName

            R.id.notifications ->
                NotificationsFragment::class.java.simpleName

            else ->
                throw IllegalStateException()
        }

    private fun getNextFragmentInstance(@IdRes menuId: Int): Fragment =
        when (menuId) {
            R.id.home ->
                HomeFragment.newInstance()

            R.id.catalog ->
                CatalogFragment.newInstance()

            R.id.profile ->
                ProfileFragment.newInstance()

            R.id.notifications ->
                NotificationsFragment.newInstance()

            else ->
                throw IllegalStateException()
        }

    //RootScreen methods
    override fun showCatalog() {
        if (navigationView.selectedItemId != R.id.catalog) {
            navigationView.selectedItemId = R.id.catalog
        }
    }

    override fun applyTransitionPrev() {
        //no-op
    }

    override fun onShowStreakSuggestion() {
        if (intent.action == LOGGED_ACTION) {
            intent.action = null

            analytic.reportEvent(Analytic.Streak.EARLY_DIALOG_SHOWN)

            StreakNotificationDialogFragment
                .newInstance(
                    title = getString(R.string.early_notification_title),
                    message = getString(R.string.early_notification_description),
                    positiveEvent = Analytic.Streak.EARLY_DIALOG_POSITIVE
                )
                .showIfNotExists(supportFragmentManager, StreakNotificationDialogFragment.TAG)
        }
    }

    override fun onTimeIntervalPicked(chosenInterval: Int) {
        analytic.reportEvent(Analytic.Streak.EARLY_NOTIFICATION_COMPLETE)
        streakPresenter.setStreakTime(chosenInterval) // we do not need attach this view, because we need only set in model
    }

    override fun onBadgeShouldBeHidden() {
        navigationView.removeBadge(R.id.notifications)
    }

    override fun onBadgeCountChanged(count: Int) {
        val badge = navigationView.getOrCreateBadge(R.id.notifications)
        badge.number = count
        badge.maxCharacterCount = 3
        badge.isVisible = true
        // TODO 09.04.2020: Uncomment after stable release of Material Components 1.2.0
//        badge.verticalOffset = 8
    }
}