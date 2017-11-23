package org.stepic.droid.ui.activities

import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.facebook.login.LoginManager
import com.vk.sdk.VKSdk
import kotlinx.android.synthetic.main.activity_main_feed.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.StepikDevicePoster
import org.stepic.droid.core.presenters.ProfileMainFeedPresenter
import org.stepic.droid.core.presenters.contracts.ProfileMainFeedView
import org.stepic.droid.model.Profile
import org.stepic.droid.ui.activities.contracts.RootScreen
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog
import org.stepic.droid.ui.fragments.CatalogFragment
import org.stepic.droid.ui.fragments.CertificatesFragment
import org.stepic.droid.ui.fragments.HomeFragment
import org.stepic.droid.ui.fragments.ProfileFragment
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.ProgressHelper
import timber.log.Timber
import javax.inject.Inject


class MainFeedActivity : BackToExitActivityWithSmartLockBase(),
        BottomNavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemReselectedListener,
        LogoutAreYouSureDialog.Companion.OnLogoutSuccessListener,
        RootScreen,
        ProfileMainFeedView {

    companion object {
        val currentIndexKey = "currentIndexKey"

        val reminderKey = "reminderKey"
        const val defaultIndex: Int = 0
        private val progressLogoutTag = "progressLogoutTag"

        const val HOME_INDEX: Int = 1
        const val CATALOG_INDEX: Int = 2
        const val PROFILE_INDEX: Int = 3
        const val CERTIFICATE_INDEX: Int = 4
    }

    @Inject
    lateinit var profileMainFeedPresenter: ProfileMainFeedPresenter

    @Inject
    lateinit var stepikDevicePoster: StepikDevicePoster

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationClickedCheck(intent)

        if (intent.hasExtra(currentIndexKey)) {
            openFragment(intent)
        }
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
                    val shortcutManager = getSystemService(ShortcutManager::class.java)
                    shortcutManager.reportShortcutUsed(AppConstants.CATALOG_SHORTCUT_ID)
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
            openFragment(intent)
        }

        profileMainFeedPresenter.attachView(this)
        if (savedInstanceState == null) {
            profileMainFeedPresenter.fetchProfile()
        }
    }

    private fun openFragment(launchIntent: Intent?) {
        setFragment(R.id.home)
        val wantedIndex = launchIntent?.getIntExtra(currentIndexKey, -1) ?: -1
        when (wantedIndex) {
            CATALOG_INDEX -> navigationView.selectedItemId = R.id.catalog
            CERTIFICATE_INDEX -> navigationView.selectedItemId = R.id.certificates
            PROFILE_INDEX -> navigationView.selectedItemId = R.id.profile
            else -> {
                //do nothing
            }
        }
    }

    private fun initNavigation() {
        navigationView.setOnNavigationItemSelectedListener(this)
        navigationView.setOnNavigationItemReselectedListener(this)
    }

    private fun showCurrentFragment(@IdRes id: Int) {
        setFragment(id)
    }

    override fun onDestroy() {
        profileMainFeedPresenter.detachView(this)
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
            R.id.certificates -> analytic.reportEvent(Analytic.Screens.USER_OPEN_CERTIFICATES)
            R.id.profile -> analytic.reportEvent(Analytic.Screens.USER_OPEN_PROFILE)
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
            }
            R.id.certificates -> {
                analytic.reportEvent(Analytic.Screens.USER_OPEN_CERTIFICATES)
                getNextFragmentOrNull(currentFragmentTag, CertificatesFragment::class.java.simpleName, CertificatesFragment.Companion::newInstance)
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
        if (currentFragmentTag == null || currentFragmentTag != nextFragmentTag) {
            return nextFragmentCreation.invoke()
        } else {
            return null
        }
    }

    private fun setFragment(@IdRes containerId: Int, fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment, fragment.javaClass.simpleName)
        fragmentTransaction.commit()
    }

    override fun onLogout() {
        profileMainFeedPresenter.logout()
    }


    //profileMainFeedView methods
    override fun showAnonymous() {
        //stub
    }

    override fun showProfile(profile: Profile) {
        //stub
    }

    override fun showLogoutLoading() {
        val loadingProgressDialogFragment = LoadingProgressDialogFragment.newInstance()
        ProgressHelper.activate(loadingProgressDialogFragment, supportFragmentManager, progressLogoutTag)
    }

    override fun onLogoutSuccess() {
        ProgressHelper.dismiss(supportFragmentManager, progressLogoutTag)
        LoginManager.getInstance().logOut()
        VKSdk.logout()
        signOutFromGoogle()
        screenManager.showLaunchScreenAfterLogout(this)
    }

    //end profileMainFeedView methods

    //RootScreen methods
    override fun showCatalog() {
        if (navigationView.selectedItemId != R.id.catalog) {
            navigationView.selectedItemId = R.id.catalog
        }
    }

    override fun applyTransitionPrev() {
        //no-op
    }
}
