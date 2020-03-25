package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main_feed.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
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
import org.stepic.droid.ui.fragments.CatalogFragment
import org.stepic.droid.ui.fragments.HomeFragment
import org.stepic.droid.ui.fragments.NotificationsFragment
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.streak.interactor.StreakInteractor
import org.stepik.android.model.Course
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

        private const val MAX_NOTIFICATION_BADGE_COUNT = 99

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

    private lateinit var navigationAdapter: AHBottomNavigationAdapter

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
            screenManager.showCourseDescription(this, course, true)
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
            CATALOG_INDEX       -> navigationView.currentItem = navigationAdapter.getPositionByMenuId(R.id.catalog)
            PROFILE_INDEX       -> navigationView.currentItem = navigationAdapter.getPositionByMenuId(R.id.profile)
            NOTIFICATIONS_INDEX -> navigationView.currentItem = navigationAdapter.getPositionByMenuId(R.id.notifications)
            else -> {
                //do nothing
            }
        }
    }

    private fun initNavigation() {
        navigationAdapter = AHBottomNavigationAdapter(this, R.menu.drawer_menu)
        navigationAdapter.setupWithBottomNavigation(navigationView)

        navigationView.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        navigationView.setOnTabSelectedListener { position, wasSelected ->
            val menuItem = navigationAdapter.getMenuItem(position)
            if (wasSelected) {
                onNavigationItemReselected(menuItem)
            } else {
                onNavigationItemSelected(menuItem)
            }
            true
        }
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
        val homeTabPosition = navigationAdapter.getPositionByMenuId(R.id.home)
        if (navigationView.currentItem == homeTabPosition) {
            finish()
            return
        } else {
            navigationView.currentItem = homeTabPosition
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
        val currentFragmentTag: String? = supportFragmentManager.findFragmentById(R.id.frame)?.tag
        val nextFragment: Fragment? = when (id) {
            R.id.home -> {
                getNextFragmentOrNull(currentFragmentTag, HomeFragment::class.java.simpleName, HomeFragment.Companion::newInstance)
            }
            R.id.catalog -> {
                getNextFragmentOrNull(currentFragmentTag, CatalogFragment::class.java.simpleName, CatalogFragment.Companion::newInstance)
            }
            R.id.profile -> {
                getNextFragmentOrNull(currentFragmentTag, ProfileFragment::class.java.simpleName, ProfileFragment.Companion::newInstance)
//                getNextFragmentOrNull(currentFragmentTag, ProfileFragment::class.java.simpleName, ProfileFragment.Companion::newInstance)
            }
            R.id.notifications -> {
                getNextFragmentOrNull(currentFragmentTag, NotificationsFragment::class.java.simpleName, NotificationsFragment::newInstance)
            }
            else -> {
                null
            }
        }
        if (nextFragment != null) {
            //animation on change fragment, not for just adding
            setFragment(R.id.frame, nextFragment)
        }
    }

    private fun getNextFragmentOrNull(currentFragmentTag: String?, nextFragmentTag: String, nextFragmentCreation: () -> Fragment): Fragment? {
        return if (currentFragmentTag == null || currentFragmentTag != nextFragmentTag) {
            nextFragmentCreation.invoke()
        } else {
            null
        }
    }

    private fun setFragment(@IdRes containerId: Int, fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment, fragment.javaClass.simpleName)
        fragmentTransaction.commit()
    }

    //RootScreen methods
    override fun showCatalog() {
        val catalogTabPosition = navigationAdapter.getPositionByMenuId(R.id.catalog)
        if (navigationView.currentItem != catalogTabPosition) {
            navigationView.currentItem = catalogTabPosition
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
        navigationView.setNotification("", navigationAdapter.getPositionByMenuId(R.id.notifications))
    }

    override fun onBadgeCountChanged(count: Int) {
        navigationView.setNotification(getBadgeStringForCount(count), navigationAdapter.getPositionByMenuId(R.id.notifications))
    }

    private fun getBadgeStringForCount(count: Int) =
        if (count > MAX_NOTIFICATION_BADGE_COUNT) {
            getString(R.string.notification_badge_placeholder)
        } else {
            count.toString()
        }
}