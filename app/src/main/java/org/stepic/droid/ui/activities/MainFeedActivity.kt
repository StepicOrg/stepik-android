package org.stepic.droid.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.view.MenuItem
import android.view.View
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.vk.sdk.VKSdk
import kotlinx.android.synthetic.main.activity_main_feed.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.ProfileMainFeedPresenter
import org.stepic.droid.core.presenters.UpdateAppPresenter
import org.stepic.droid.core.presenters.contracts.ProfileMainFeedView
import org.stepic.droid.core.presenters.contracts.UpdateAppView
import org.stepic.droid.model.Profile
import org.stepic.droid.notifications.StepicInstanceIdService
import org.stepic.droid.services.UpdateWithApkService
import org.stepic.droid.ui.activities.contracts.BottomNavigationViewRoot
import org.stepic.droid.ui.activities.contracts.RootScreen
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog
import org.stepic.droid.ui.dialogs.NeedUpdatingDialog
import org.stepic.droid.ui.fragments.FindCoursesFragment
import org.stepic.droid.ui.fragments.MyCoursesFragment
import org.stepic.droid.ui.fragments.ProfileFragment
import org.stepic.droid.ui.util.BottomNavigationBehavior
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.ProgressHelper
import timber.log.Timber
import javax.inject.Inject


class MainFeedActivity : BackToExitActivityBase(),
        BottomNavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemReselectedListener,
        LogoutAreYouSureDialog.Companion.OnLogoutSuccessListener,
        RootScreen,
        BottomNavigationViewRoot,
        UpdateAppView,
        ProfileMainFeedView {
    companion object {

        val currentIndexKey = "currentIndexKey"

        val reminderKey = "reminderKey"
        const val defaultIndex: Int = 0
        val defaultTag: String = MyCoursesFragment::class.java.simpleName
        private val progressLogoutTag = "progressLogoutTag"
        // FIXME: 10.08.17 remove it
        val certificateFragmentIndex: Int
            get() = 4
        // FIXME: 10.08.17 remove it
        val myCoursesIndex: Int
            get() = 1

        // FIXME: 10.08.17 remove it
        val findCoursesIndex: Int
            get() = 2

        val profileIndex: Int
            get() = 3

    }

    @Inject
    lateinit var updateAppPresenter: UpdateAppPresenter

    @Inject
    lateinit var profileMainFeedPresenter: ProfileMainFeedPresenter

    private var googleApiClient: GoogleApiClient? = null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationClickedCheck(intent)
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
                sharedPreferenceHelper.clickEnrollNotification(DateTime.now(DateTimeZone.getDefault()).millis)
            } else if (action == AppConstants.OPEN_NOTIFICATION_FROM_STREAK) {
                sharedPreferenceHelper.resetNumberOfStreakNotifications()
                analytic.reportEvent(Analytic.Streak.STREAK_NOTIFICATION_OPENED)
            } else if (action == AppConstants.OPEN_SHORTCUT_FIND_COURSES) {
                analytic.reportEvent(Analytic.Shortcut.FIND_COURSES)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                    val shortcutManager = getSystemService(ShortcutManager::class.java)
                    shortcutManager.reportShortcutUsed(AppConstants.FIND_COURSES_SHORTCUT_ID)
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
        if (checkPlayServices()) {
            initGoogleApiClient()
        }
        initNavigation()


        updateAppPresenter.attachView(this)
        updateAppPresenter.checkForUpdate()

        if (checkPlayServices() && !sharedPreferenceHelper.isGcmTokenOk) {
            threadPoolExecutor.execute {
                StepicInstanceIdService.updateAnywhere(api, sharedPreferenceHelper, analytic) //FCM!
            }
        }

        val course = courseFromExtra
        if (course != null) {
            intent.removeExtra(AppConstants.KEY_COURSE_BUNDLE)
            screenManager.showCourseDescription(this, course, true)
        }

        if (savedInstanceState == null) {
            setFragment(R.id.my_courses)
            val wantedIndex = intent?.getIntExtra(currentIndexKey, -1) ?: -1
            when (wantedIndex) {
                findCoursesIndex -> navigationView.selectedItemId = R.id.find_courses
                else -> {
                    //do nothing
                }
            }
        }

        profileMainFeedPresenter.attachView(this)
    }

    private fun initGoogleApiClient() {
        val serverClientId = config.googleServerClientId
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(Scopes.EMAIL), Scope(Scopes.PROFILE))
                .requestServerAuthCode(serverClientId)
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, {} /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build()
    }


    private fun initNavigation() {
        navigationView.setOnNavigationItemSelectedListener(this)
        navigationView.setOnNavigationItemReselectedListener(this)
    }

    private fun showCurrentFragment(@IdRes id: Int) {
        setFragment(id)
    }

    override fun onDestroy() {
        updateAppPresenter.detachView(this)
        profileMainFeedPresenter.detachView(this)
        if (isFinishing) {
            App.componentManager().releaseMainFeedComponent()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (navigationView.selectedItemId == R.id.my_courses) {
            finish()
            return
        }

        //avoid memory leak, when user click back:
        val fragment = supportFragmentManager.findFragmentById(R.id.frame)
        supportFragmentManager.popBackStackImmediate()
        supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit()
        navigationView.selectedItemId = R.id.my_courses
        showBottomBar()
    }


    override fun onStart() {
        super.onStart()
        showBottomBar(false)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE) {
            val permissionExternalStorage = permissions[0] ?: return

            if (permissionExternalStorage == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val link = sharedPreferenceHelper.tempLink
                if (link != null) {
                    val updateIntent = Intent(this, UpdateWithApkService::class.java)
                    updateIntent.putExtra(UpdateWithApkService.linkKey, link)
                    this.startService(updateIntent)
                }
            }
        }
    }

    override fun onNeedUpdate(linkForUpdate: String?, isAppInGp: Boolean) {
        if (isAppInGp && linkForUpdate == null) {
            return
        }
        val storedTimestamp = sharedPreferenceHelper.lastShownUpdatingMessageTimestamp
        val needUpdate = DateTimeHelper.isNeededUpdate(storedTimestamp, AppConstants.MILLIS_IN_24HOURS)
        if (!needUpdate) return

        sharedPreferenceHelper.storeLastShownUpdatingMessage()
        analytic.reportEvent(Analytic.Interaction.UPDATING_MESSAGE_IS_SHOWN)
        val dialog = NeedUpdatingDialog.newInstance(linkForUpdate, isAppInGp)
        if (!dialog.isAdded) {
            dialog.show(supportFragmentManager, null)
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
            R.id.my_courses -> analytic.reportEvent(Analytic.Screens.USER_OPEN_MY_COURSES)
            R.id.find_courses -> {
                analytic.reportEvent(Analytic.Screens.USER_OPEN_FIND_COURSES)
                if (sharedPreferenceHelper.authResponseFromStore == null) {
                    analytic.reportEvent(Analytic.Anonymous.BROWSE_COURSES_DRAWER)
                }
            }
//            R.id.certificates -> analytic.reportEvent(Analytic.Screens.USER_OPEN_CERTIFICATES);
            R.id.profile -> analytic.reportEvent(Analytic.Screens.USER_OPEN_PROFILE)
        }
    }

    private fun setFragment(@IdRes id: Int) {
        val currentFragmentTag: String? = supportFragmentManager.findFragmentById(R.id.frame)?.tag
        val nextFragment: Fragment? = when (id) {
            R.id.my_courses -> {
                getNextFragmentOrNull(currentFragmentTag, MyCoursesFragment::class.java.simpleName, MyCoursesFragment.Companion::newInstance)
            }
            R.id.find_courses -> {
                getNextFragmentOrNull(currentFragmentTag, FindCoursesFragment::class.java.simpleName, FindCoursesFragment::newInstance)
            }
            R.id.profile -> {
                getNextFragmentOrNull(currentFragmentTag, ProfileFragment::class.java.simpleName, ProfileFragment.Companion::newInstance)
            }
            else -> {
                null
            }
        }
        if (nextFragment != null) {
            //animation on change fragment, not for just adding
            val needAnimation = currentFragmentTag != null
            setFragment(R.id.frame, nextFragment, needAnimation)
        }
    }

    private fun getNextFragmentOrNull(currentFragmentTag: String?, nextFragmentTag: String, nextFragmentCreation: () -> Fragment): Fragment? {
        if (currentFragmentTag == null || currentFragmentTag != nextFragmentTag) {
            return nextFragmentCreation.invoke()
        } else {
            return null
        }
    }

    private fun showBottomBar(needAnimation: Boolean = true) {
        val params = navigationView.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as? BottomNavigationBehavior
        behavior?.showBottomBar(navigationView, needAnimation)
    }

    private fun setFragment(@IdRes containerId: Int, fragment: Fragment, needAnimation: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (needAnimation) {
            //do not use fade_in - fade_out animation, because it is looking as lag
            //the second problem is null background for window
//            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        }
        fragmentTransaction.replace(containerId, fragment, fragment.javaClass.simpleName)
        val countInBackStack = supportFragmentManager.backStackEntryCount
        val isRootScreen = defaultTag == fragment.javaClass.simpleName

        if (isRootScreen && countInBackStack < 1 || !isRootScreen && countInBackStack < 2) {
            fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        }
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
        if (googleApiClient?.isConnected ?: false) {
            Auth.GoogleSignInApi.signOut(googleApiClient)
        }
        screenManager.showLaunchScreenAfterLogout(this)
    }

    //end profileMainFeedView methods

    //RootScreen methods
    override fun showFindCourses() {
        if (navigationView.selectedItemId != R.id.find_courses) {
            navigationView.selectedItemId = R.id.find_courses
        }
    }

    override fun disableAnyBehaviour() {
        val params = navigationView.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = null
    }

    override fun resetDefaultBehaviour() {
        val params = navigationView.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = BottomNavigationBehavior<View>()
    }

}
