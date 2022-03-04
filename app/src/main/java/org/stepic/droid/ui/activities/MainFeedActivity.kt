package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_main_feed.*
import org.stepic.droid.BuildConfig
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.OnboardingSplitTestVersion2
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.configuration.RemoteConfig
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
import org.stepik.android.domain.base.analytic.BUNDLEABLE_ANALYTIC_EVENT
import org.stepik.android.domain.base.analytic.toGenericAnalyticEvent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.streak.interactor.StreakInteractor
import org.stepik.android.model.Course
import org.stepik.android.view.catalog.ui.fragment.CatalogFragment
import org.stepik.android.view.course_list.notification.RemindAppNotificationDelegate
import org.stepik.android.view.course_list.routing.getCourseListCollectionId
import org.stepik.android.view.debug.ui.fragment.DebugMenu
import org.stepik.android.view.profile.ui.fragment.ProfileFragment
import org.stepik.android.view.story_deeplink.routing.getStoryId
import org.stepik.android.view.story_deeplink.ui.dialog.StoryDeepLinkDialogFragment
import org.stepik.android.view.streak.notification.StreakNotificationDelegate
import org.stepik.android.view.streak.ui.dialog.StreakNotificationDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists
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

        const val defaultIndex: Int = 0
        private const val LOGGED_ACTION = "LOGGED_ACTION"

        private const val STORY_DEEPLINK = "story-template"
        private const val CATALOG_DEEPLINK = "catalog"
        private const val NOTIFICATIONS_DEEPLINK = "notifications"

        private const val DEBUG_BUILD_TYPE = "debug"
        private const val STAGE_DEBUGGABLE_BUILD_TYPE = "stageDebuggable"

        const val HOME_INDEX: Int = 1
        const val CATALOG_INDEX: Int = 2
        const val PROFILE_INDEX: Int = 3
        const val NOTIFICATIONS_INDEX: Int = 4
        const val DEBUG_INDEX: Int = 5

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

    @Inject
    internal lateinit var remoteConfig: FirebaseRemoteConfig

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var onboardingSplitTestVersion2: OnboardingSplitTestVersion2

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkShortcutAction(intent)

        openFragment(intent)
    }

    /**
     * We call this only from onCreate, because all the notifications launch MainFeedActivity through FLAG_ACTIVITY_NEW_TASK
     */
    private fun checkNotificationClick(intent: Intent) {
        val action = intent.action
        if (action != null) {
            when (action) {
                RemindAppNotificationDelegate.REMIND_APP_NOTIFICATION_CLICKED ->
                    sharedPreferenceHelper.clickEnrollNotification(DateTimeHelper.nowUtc())

                StreakNotificationDelegate.STREAK_NOTIFICATION_CLICKED ->
                    sharedPreferenceHelper.resetNumberOfStreakNotifications()
            }

            //after tracking check on null user
            if (sharedPreferenceHelper.authResponseFromStore == null) {
                screenManager.openSplash(this)
            }
        }
    }

    private fun checkShortcutAction(intent: Intent) {
        val action = intent.action
        if (action != null) {
            if (action == AppConstants.OPEN_SHORTCUT_CATALOG) {
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

        if (savedInstanceState == null) {
            checkShortcutAction(intent)
            checkNotificationClick(intent)
            val analyticEvent = intent
                .getBundleExtra(BUNDLEABLE_ANALYTIC_EVENT)
                ?.toGenericAnalyticEvent()

            if (analyticEvent != null) {
                analytic.report(analyticEvent)
            }
        }

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
        handleShowPersonalizedOnboarding()
        handlePersonalizedCourseList()
    }

    private fun openFragment(launchIntent: Intent?, forceHome: Boolean = false) {
        if (forceHome) {
            setFragment(R.id.home)
        }

        when (getFragmentIndexFromIntent(launchIntent)) {
            HOME_INDEX -> {
                navigationView.selectedItemId = R.id.home
                val storyId = launchIntent?.getStoryId()
                if (storyId != null) {
                    StoryDeepLinkDialogFragment
                        .newInstance(storyId, launchIntent.dataString ?: "")
                        .showIfNotExists(supportFragmentManager, StoryDeepLinkDialogFragment.TAG)
                }
            }
            CATALOG_INDEX -> {
                navigationView.selectedItemId = R.id.catalog
                launchIntent?.data?.pathSegments?.let {
                    when {
                        it.contains(CATALOG_DEEPLINK) -> {
                            val courseCollectionId = launchIntent.getCourseListCollectionId()
                            if (courseCollectionId != null) {
                                screenManager.showCoursesCollection(this, courseCollectionId)
                            }
                        }
                        it.contains(STORY_DEEPLINK) -> {
                            val storyId = launchIntent.getStoryId()
                            if (storyId != null) {
                                StoryDeepLinkDialogFragment
                                    .newInstance(storyId, launchIntent.dataString ?: "")
                                    .showIfNotExists(
                                        supportFragmentManager,
                                        StoryDeepLinkDialogFragment.TAG
                                    )
                            }
                        }
                    }
                }
            }
            PROFILE_INDEX -> navigationView.selectedItemId = R.id.profile
            NOTIFICATIONS_INDEX -> navigationView.selectedItemId = R.id.notifications
            DEBUG_INDEX -> navigationView.selectedItemId = R.id.debug
            else -> {
                //do nothing
            }
        }
    }

    private fun getFragmentIndexFromIntent(intent: Intent?): Int {
        if (intent == null) return -1

        intent.data?.pathSegments?.let {
            when {
                it.contains(NOTIFICATIONS_DEEPLINK) -> return NOTIFICATIONS_INDEX
                it.contains(CATALOG_DEEPLINK)       -> return CATALOG_INDEX
                it.contains(STORY_DEEPLINK) -> {
                    return if (remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED)) {
                        HOME_INDEX
                    } else {
                        CATALOG_INDEX
                    }
                }
                else -> {}
            }
        }

        return intent.getIntExtra(CURRENT_INDEX_KEY, -1)
    }

    private fun initNavigation() {
        navigationView.setOnNavigationItemSelectedListener(::onNavigationItemSelected)
        navigationView.setOnNavigationItemReselectedListener(::onNavigationItemReselected)
        navigationView.menu.findItem(R.id.debug).isVisible = BuildConfig.BUILD_TYPE == DEBUG_BUILD_TYPE || BuildConfig.BUILD_TYPE == STAGE_DEBUGGABLE_BUILD_TYPE
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
        showCurrentFragment(menuItem.itemId)
        return true
    }

    override fun onNavigationItemReselected(menuItem: MenuItem) {
//        scrollUp()
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
                HomeFragment.TAG

            R.id.catalog ->
                CatalogFragment.TAG

            R.id.profile ->
                ProfileFragment.TAG

            R.id.notifications ->
                NotificationsFragment.TAG

            R.id.debug ->
                DebugMenu.TAG


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

            R.id.debug ->
                DebugMenu.newInstance()

            else ->
                throw IllegalStateException()
        }

    // TODO APPS-3292: Refactor this if AB test is successful
    private fun handleShowPersonalizedOnboarding() {
        if (sharedPreferenceHelper.isPersonalizedOnboardingWasShown ||
            onboardingSplitTestVersion2.currentGroup == OnboardingSplitTestVersion2.Group.Control) {
                return
        }
        screenManager.showPersonalizedOnboarding(this)
    }
    private fun handlePersonalizedCourseList() {
        val personalizedCourseList = sharedPreferenceHelper
            .personalizedCourseList
            .takeIf { it != -1L }
            ?: return
        sharedPreferenceHelper.personalizedCourseList = -1L
        screenManager.showCoursesCollection(this, personalizedCourseList)
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
        badge.verticalOffset = 8
    }
}