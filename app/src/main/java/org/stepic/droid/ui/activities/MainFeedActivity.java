package org.stepic.droid.ui.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.squareup.otto.Subscribe;
import com.vk.sdk.VKSdk;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.profile.ProfileCanBeShownEvent;
import org.stepic.droid.events.updating.NeedUpdateEvent;
import org.stepic.droid.model.EmailAddress;
import org.stepic.droid.model.Profile;
import org.stepic.droid.notifications.StepicInstanceIdService;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.services.UpdateAppService;
import org.stepic.droid.services.UpdateWithApkService;
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog;
import org.stepic.droid.ui.dialogs.NeedUpdatingDialog;
import org.stepic.droid.ui.fragments.CertificateFragment;
import org.stepic.droid.ui.fragments.DownloadsFragment;
import org.stepic.droid.ui.fragments.FindCoursesFragment;
import org.stepic.droid.ui.fragments.MyCoursesFragment;
import org.stepic.droid.ui.fragments.NotificationsFragment;
import org.stepic.droid.ui.util.BackButtonHandler;
import org.stepic.droid.ui.util.LogoutSuccess;
import org.stepic.droid.ui.util.OnBackClickListener;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.ProfileExtensionKt;
import org.stepic.droid.web.EmailAddressResponse;
import org.stepic.droid.web.StepicProfileResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class MainFeedActivity extends BackToExitActivityBase
        implements NavigationView.OnNavigationItemSelectedListener, LogoutSuccess, BackButtonHandler, HasDrawer {
    public static final String KEY_CURRENT_INDEX = "Current_index";
    private static final int REQUEST_INVITE_CODE = 120;
    public static final String REMINDER_KEY = "reminder_key";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;

    ImageView profileImage;

    TextView userNameTextView;

    @BindString(R.string.my_courses_title)
    String coursesTitle;

    @BindDrawable(R.drawable.placeholder_icon)
    Drawable userPlaceholder;

    private int currentIndex;

    GoogleApiClient googleApiClient;

    private List<WeakReference<OnBackClickListener>> onBackClickListenerList = new ArrayList<>(8);
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        notificationClickedCheck(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            initFragments(extras);
        }
    }

    private void notificationClickedCheck(Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(AppConstants.OPEN_NOTIFICATION)) {
                analytic.reportEvent(AppConstants.OPEN_NOTIFICATION);
            } else if (intent.getAction().equals(AppConstants.OPEN_NOTIFICATION_FOR_ENROLL_REMINDER)) {
                String dayTypeString = intent.getStringExtra(REMINDER_KEY);
                if (dayTypeString == null){
                    dayTypeString = "";
                }
                analytic.reportEvent(Analytic.Notification.REMIND_OPEN, dayTypeString);
                Timber.d(Analytic.Notification.REMIND_OPEN);
                sharedPreferenceHelper.clickEnrollNotification(DateTime.now(DateTimeZone.getDefault()).getMillis());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);
        unbinder = ButterKnife.bind(this);
        notificationClickedCheck(getIntent());
        initGoogleApiClient();
        initDrawerHeader();
        setUpToolbar();
        setUpDrawerLayout();
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null) {
            initFragments(savedInstanceState);
        } else {
            initFragments(extras);
        }

        bus.register(this);

        final SharedPreferenceHelper helper = shell.getSharedPreferenceHelper();
        Profile cachedProfile = helper.getProfile();
        if (cachedProfile != null) {
            showProfile(new ProfileCanBeShownEvent(cachedProfile));//update now!
        }
        shell.getApi().getUserProfile().enqueue(new Callback<StepicProfileResponse>() {
            @Override
            public void onResponse(Response<StepicProfileResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    Profile profile = response.body().getProfile();
                    final long[] emailIds = profile.getEmailAddresses();
                    if (emailIds != null && emailIds.length != 0) {
                        shell.getApi().getEmailAddresses(emailIds).enqueue(new Callback<EmailAddressResponse>() {
                            @Override
                            public void onResponse(Response<EmailAddressResponse> response, Retrofit retrofit) {
                                if (response.isSuccess()) {
                                    EmailAddressResponse emailsResponse = response.body();
                                    if (emailsResponse != null) {
                                        List<EmailAddress> emails = emailsResponse.getEmailAddresses();
                                        if (emails != null && !emails.isEmpty()) {
                                            helper.storeEmailAddresses(emails);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {

                            }
                        });
                    }

                    helper.storeProfile(profile);
                    bus.post(new ProfileCanBeShownEvent(profile));//show if we can
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // FIXME: 06.10.15 Sometimes profile is not load, investigate it! (maybe just set for update when create this activity)
                //do nothing because view is now visible

            }
        });


        if (checkPlayServices() && !sharedPreferenceHelper.isGcmTokenOk()) {

            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    StepicInstanceIdService.Companion.updateAnywhere(shell.getApi(), sharedPreferenceHelper, analytic); //FCM!
                }
            });
        }

        Intent updateIntent = new Intent(this, UpdateAppService.class);
        startService(updateIntent);
    }

    private void initGoogleApiClient() {
        String serverClientId = config.getGoogleServerClientId();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.EMAIL), new Scope(Scopes.PROFILE))
                .requestServerAuthCode(serverClientId)
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainApplication.getAppContext(), R.string.connectionProblems, Toast.LENGTH_SHORT).show();
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void initDrawerHeader() {
        View headerLayout = navigationView.getHeaderView(0);
        profileImage = ButterKnife.findById(headerLayout, R.id.profile_image);
        userNameTextView = ButterKnife.findById(headerLayout, R.id.username);

        profileImage.setVisibility(View.INVISIBLE);
        userNameTextView.setVisibility(View.INVISIBLE);
        userNameTextView.setText("");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            fragmentBackKeyIntercept();
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.frame);
            fragmentManager.popBackStackImmediate();
            fragmentManager.beginTransaction().remove(fragment).commit();
            if (currentIndex == 0 || fragmentManager.getBackStackEntryCount() <= 0) {
                finish();
            } else {
                currentIndex = 0;
                navigationView.setCheckedItem(R.id.my_courses);
                setTitle(R.string.my_courses_title);
            }
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initFragments(Bundle bundle) {
        if (bundle == null) {
            currentIndex = 0;
        } else {
            currentIndex = bundle.getInt(KEY_CURRENT_INDEX);
        }

        showCurrentFragment(currentIndex);
    }

    private void showCurrentFragment(int currentIndex) {
        this.currentIndex = currentIndex;
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.getItem(currentIndex);
        menuItem.setChecked(true); //when we do not choose in menu
        showCurrentFragment(menuItem);
    }

    private void showCurrentFragment(MenuItem menuItem) {
        setTitle(menuItem.getTitle());
        setFragment(menuItem);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        sendOpenUserAnalytic(menuItem.getItemId());
        switch (menuItem.getItemId()) {
            case R.id.logout_item:
                analytic.reportEvent(Analytic.Interaction.CLICK_LOGOUT);

                LogoutAreYouSureDialog dialog = LogoutAreYouSureDialog.newInstance();
                if (!dialog.isAdded()) {
                    dialog.show(getSupportFragmentManager(), null);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawers();
                    }
                }, 0);
                return true;
            case R.id.my_settings:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawers();
                    }
                }, 0);
                shell.getScreenProvider().showSettings(this);
                return true;
            case R.id.feedback:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawers();
                    }
                }, 0);
                shell.getScreenProvider().openFeedbackActivity(this);
                return true;
            case R.id.invite:
                shell.getScreenProvider().inviteFriend(this, REQUEST_INVITE_CODE);
                break;
            default:
                showCurrentFragment(menuItem);
                break;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawers();
            }
        }, 0);
        return true;
    }

    private void sendOpenUserAnalytic(int itemId) {
        switch (itemId) {
            case R.id.logout_item:
                analytic.reportEvent(Analytic.Screens.USER_LOGOUT);
                break;
            case R.id.my_settings:
                analytic.reportEvent(Analytic.Screens.USER_OPEN_SETTINGS);
                break;
            case R.id.my_courses:
                analytic.reportEvent(Analytic.Screens.USER_OPEN_MY_COURSES);
                break;
            case R.id.find_lessons:
                analytic.reportEvent(Analytic.Screens.USER_OPEN_FIND_COURSES);
                break;
            case R.id.cached_videos:
                analytic.reportEvent(Analytic.Screens.USER_OPEN_DOWNLOADS);
                break;
            case R.id.feedback:
                analytic.reportEvent(Analytic.Screens.USER_OPEN_FEEDBACK);
                break;
            case R.id.certificates:
                analytic.reportEvent(Analytic.Screens.USER_OPEN_CERTIFICATES);
                break;
            case R.id.notifications:
                analytic.reportEvent(Analytic.Screens.USER_OPEN_NOTIFICATIONS);
                break;
            case R.id.invite:
                analytic.reportEvent(Analytic.Screens.USER_OPEN_INVITE);
                break;
        }
    }

    private void setUpDrawerLayout() {
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_closed);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFragment(MenuItem menuItem) {
        fragmentBackKeyIntercept(); //on back when fragment is changed (work for filter feature)
        Fragment shortLifetimeRef = null;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame);
        String tag = null;
        if (fragment != null) {
            tag = fragment.getTag();
        }
        switch (menuItem.getItemId()) {
            case R.id.my_courses:
                currentIndex = 1;
                if (tag == null || !tag.equals(MyCoursesFragment.class.toString())) {
                    shortLifetimeRef = MyCoursesFragment.newInstance();
                }
                break;
            case R.id.find_lessons:
                currentIndex = 2;
                if (tag == null || !tag.equals(FindCoursesFragment.class.toString())) {
                    shortLifetimeRef = FindCoursesFragment.newInstance();
                }
                break;
            case R.id.cached_videos:
                currentIndex = 3;
                if (tag == null || !tag.equals(DownloadsFragment.class.toString())) {
                    shortLifetimeRef = DownloadsFragment.newInstance();
                }
                break;

            case R.id.certificates:
                currentIndex = 4;
                if (tag == null || !tag.equals(CertificateFragment.class.toString())) {
                    shortLifetimeRef = CertificateFragment.newInstance();
                }
                break;
            case R.id.notifications:
                currentIndex = 5;
                if (tag == null || !tag.equals(NotificationsFragment.class.toString())) {
                    shortLifetimeRef = NotificationsFragment.newInstance();
                }
                break;
        }
        currentIndex--; // menu indices from 1
        if (shortLifetimeRef != null) {

            if (fragment != null) {
                String before = fragment.getTag();
                String now = shortLifetimeRef.getClass().getSimpleName();
                if (!before.equals(now)) {
                    setFragment(R.id.frame, shortLifetimeRef);
                }
            } else {
                setFragment(R.id.frame, shortLifetimeRef);
            }
        }

    }

    @Subscribe
    public void showProfile(ProfileCanBeShownEvent e) {
        Profile profile = e.getProfile();
        if (profile == null) {
            analytic.reportError(Analytic.Error.NULL_SHOW_PROFILE, new NullPointerException());
            return;
        }
        profileImage.setVisibility(View.VISIBLE);
        userNameTextView.setVisibility(View.VISIBLE);
        Glide
                .with(MainFeedActivity.this)
                .load(profile.getAvatar())
                .asBitmap()
                .placeholder(userPlaceholder)
                .into(profileImage);
        userNameTextView.setText(ProfileExtensionKt.getFirstAndLastName(profile));
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shell.getScreenProvider().openProfile(MainFeedActivity.this);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_INDEX, currentIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        drawerLayout.removeDrawerListener(actionBarDrawerToggle);
        profileImage.setOnClickListener(null);
        super.onDestroy();
    }

    public void showFindLesson() {
        currentIndex = 1;
        showCurrentFragment(currentIndex);
    }

    public static int getFindLessonIndex() {
        return 1;
    }


    @Subscribe
    public void needUpdateCallback(NeedUpdateEvent event) {

        if (!event.isAppInGp() && event.getLinkForUpdate() == null) {
            return;
        }
        long storedTimestamp = sharedPreferenceHelper.getLastShownUpdatingMessageTimestamp();
        boolean needUpdate = DateTimeHelper.INSTANCE.isNeededUpdate(storedTimestamp, AppConstants.MILLIS_IN_24HOURS);
        if (!needUpdate) return;

        sharedPreferenceHelper.storeLastShownUpdatingMessage();
        analytic.reportEvent(Analytic.Interaction.UPDATING_MESSAGE_IS_SHOWN);
        DialogFragment dialog = NeedUpdatingDialog.Companion.newInstance(event.getLinkForUpdate(), event.isAppInGp());
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String link = sharedPreferenceHelper.getTempLink();
                if (link != null) {
                    Intent updateIntent = new Intent(this, UpdateWithApkService.class);
                    updateIntent.putExtra(UpdateWithApkService.Companion.getLinkKey(), link);
                    this.startService(updateIntent);
                }
            }
        }
    }

    public static int getCertificateFragmentIndex() {
        return 3;
    }

    public static int getDownloadFragmentIndex() {
        return 2;
    }

    @Override
    public void onLogout() {
        LoginManager.getInstance().logOut();
        VKSdk.logout();
        Auth.GoogleSignInApi.signOut(googleApiClient);
        sharedPreferenceHelper.deleteAuthInfo();
        shell.getScreenProvider().showLaunchScreen(MainApplication.getAppContext(), false);
    }

    private boolean fragmentBackKeyIntercept() {
        if (onBackClickListenerList != null) {
            for (WeakReference<OnBackClickListener> weakReference : onBackClickListenerList) {
                if (weakReference != null) {
                    OnBackClickListener listener = weakReference.get();
                    if (listener != null) {
                        listener.onBackClick();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setBackClickListener(@NotNull OnBackClickListener onBackClickListener) {
        this.onBackClickListenerList.add(new WeakReference<>(onBackClickListener));
    }

    @Override
    public void removeBackClickListener(@NotNull OnBackClickListener onBackClickListener) {
        for (Iterator<WeakReference<OnBackClickListener>> iterator = onBackClickListenerList.iterator();
             iterator.hasNext(); ) {
            WeakReference<OnBackClickListener> weakRef = iterator.next();
            if (weakRef.get() == onBackClickListener) {
                iterator.remove();
            }
        }
    }

    @Override
    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INVITE_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    analytic.reportEvent(Analytic.Invite.INVITE_SENT, id);
                }
            } else {
                analytic.reportEvent(Analytic.Invite.INVITE_CANCELED);
            }
        }
    }
}
