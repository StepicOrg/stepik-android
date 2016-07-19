package org.stepic.droid.view.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.events.profile.ProfileCanBeShownEvent;
import org.stepic.droid.events.updating.NeedUpdateEvent;
import org.stepic.droid.model.EmailAddress;
import org.stepic.droid.model.Profile;
import org.stepic.droid.notifications.StepicInstanceIdService;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.services.UpdateAppService;
import org.stepic.droid.services.UpdateWithApkService;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.view.dialogs.LogoutAreYouSureDialog;
import org.stepic.droid.view.dialogs.NeedUpdatingDialog;
import org.stepic.droid.view.fragments.DownloadsFragment;
import org.stepic.droid.view.fragments.FeedbackFragment;
import org.stepic.droid.view.fragments.FindCoursesFragment;
import org.stepic.droid.view.fragments.MyCoursesFragment;
import org.stepic.droid.web.EmailAddressResponse;
import org.stepic.droid.web.StepicProfileResponse;

import java.util.List;

import butterknife.BindView;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainFeedActivity extends BackToExitActivityBase
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String KEY_CURRENT_INDEX = "Current_index";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    ImageView mProfileImage;

    TextView mUserNameTextView;


    @BindString(R.string.my_courses_title)
    String mCoursesTitle;

    @BindDrawable(R.drawable.placeholder_icon)
    Drawable mUserPlaceholder;

    private int mCurrentIndex;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            initFragments(extras);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);
        unbinder = ButterKnife.bind(this);

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

        final SharedPreferenceHelper helper = mShell.getSharedPreferenceHelper();
        Profile cachedProfile = helper.getProfile();
        if (cachedProfile != null) {
            showProfile(new ProfileCanBeShownEvent(cachedProfile));//update now!
        }
        mShell.getApi().getUserProfile().enqueue(new Callback<StepicProfileResponse>() {
            @Override
            public void onResponse(Response<StepicProfileResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    Profile profile = response.body().getProfile();
                    final long[] emailIds = profile.getEmailAddresses();
                    if (emailIds != null && emailIds.length != 0) {
                        mShell.getApi().getEmailAddresses(emailIds).enqueue(new Callback<EmailAddressResponse>() {
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


        if (checkPlayServices() && !mSharedPreferenceHelper.isGcmTokenOk()) {

            mThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    StepicInstanceIdService.Companion.updateAnywhere(mShell.getApi(), mSharedPreferenceHelper, analytic); //FCM!
                }
            });
        }

        Intent updateIntent = new Intent(this, UpdateAppService.class);
        startService(updateIntent);
    }

    private void initDrawerHeader() {
        View headerLayout = mNavigationView.getHeaderView(0);
        mProfileImage = ButterKnife.findById(headerLayout, R.id.profile_image);
        mUserNameTextView = ButterKnife.findById(headerLayout, R.id.username);

        mProfileImage.setVisibility(View.INVISIBLE);
        mUserNameTextView.setVisibility(View.INVISIBLE);
        mUserNameTextView.setText("");
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initFragments(Bundle bundle) {
        if (bundle == null) {
            mCurrentIndex = 0;
        } else {
            mCurrentIndex = bundle.getInt(KEY_CURRENT_INDEX);
        }

        showCurrentFragment(mCurrentIndex);
    }

    private void showCurrentFragment(int currentIndex) {
        mCurrentIndex = currentIndex;
        Menu menu = mNavigationView.getMenu();
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
                        mDrawerLayout.closeDrawers();
                    }
                }, 0);
                return true;
            case R.id.my_settings:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                }, 0);
                mShell.getScreenProvider().showSettings(this);
                return true;

            default:
                showCurrentFragment(menuItem);
                break;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawers();
            }
        }, 0);
        return true;
    }

    private void setUpDrawerLayout() {
        mNavigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_closed);
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFragment(MenuItem menuItem) {
        Fragment shortLifetimeRef = null;
        boolean isFragment = true;
        switch (menuItem.getItemId()) {
            case R.id.my_courses:
                mCurrentIndex = 1;
                shortLifetimeRef = MyCoursesFragment.newInstance();
                break;
            case R.id.find_lessons:
                mCurrentIndex = 2;
                shortLifetimeRef = FindCoursesFragment.newInstance();
                break;
            case R.id.cached_videos:
                mCurrentIndex = 3;
                shortLifetimeRef = DownloadsFragment.newInstance();
                break;
            case R.id.feedback:
                mCurrentIndex = 5;
                shortLifetimeRef = FeedbackFragment.Companion.newInstance();
                break;
        }
        if (isFragment) {
            mCurrentIndex--; // menu indices from 1
            if (shortLifetimeRef != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame);
                if (fragment != null) {
                    String before = fragment.getTag();
                    String now = shortLifetimeRef.getClass().toString();
                    if (!before.equals(now)) {
                        setFragment(R.id.frame, shortLifetimeRef);
                    }
                } else {
                    setFragment(R.id.frame, shortLifetimeRef);
                }
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
        mProfileImage.setVisibility(View.VISIBLE);
        mUserNameTextView.setVisibility(View.VISIBLE);
        Picasso.with(MainFeedActivity.this).load(profile.getAvatar()).
                placeholder(mUserPlaceholder).error(mUserPlaceholder).into(mProfileImage);
        mUserNameTextView.setText(profile.getFirst_name() + " " + profile.getLast_name());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_INDEX, mCurrentIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    public void showFindLesson() {
        mCurrentIndex = 1;
        showCurrentFragment(mCurrentIndex);
    }

    public static int getFindLessonIndex () {
        return 1;
    }


    @Subscribe
    public void needUpdateCallback(NeedUpdateEvent event) {

        if (!event.isAppInGp() && event.getLinkForUpdate() == null) {
            return;
        }
        long storedTimestamp = mSharedPreferenceHelper.getLastShownUpdatingMessageTimestamp();
        boolean needUpdate = DateTimeHelper.INSTANCE.isNeededUpdate(storedTimestamp, AppConstants.MILLIS_IN_24HOURS);
        if (!needUpdate) return;

        mSharedPreferenceHelper.storeLastShownUpdatingMessage();
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
                String link = mSharedPreferenceHelper.getTempLink();
                if (link != null) {
                    Intent updateIntent = new Intent(this, UpdateWithApkService.class);
                    updateIntent.putExtra(UpdateWithApkService.Companion.getLinkKey(), link);
                    this.startService(updateIntent);
                }
            }
        }
    }

    public static int getDownloadFragmentIndex(){
        return 2;
    }
}
