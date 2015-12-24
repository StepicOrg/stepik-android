package org.stepic.droid.view.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
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

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.events.profile.ProfileCanBeShownEvent;
import org.stepic.droid.model.Profile;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.dialogs.LogoutAreYouSureDialog;
import org.stepic.droid.view.fragments.DownloadsFragment;
import org.stepic.droid.view.fragments.FindCoursesFragment;
import org.stepic.droid.view.fragments.MyCoursesFragment;
import org.stepic.droid.view.fragments.SettingsFragment;
import org.stepic.droid.web.StepicProfileResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainFeedActivity extends FragmentActivityBase
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String KEY_CURRENT_INDEX = "Current_index";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.navigation_view)
    NavigationView mNavigationView;

    @Bind(R.id.drawer)
    DrawerLayout mDrawerLayout;


    //    @Bind(R.id.profile_image)
    ImageView mProfileImage;

    //    @Bind(R.id.username)
    TextView mUserNameTextView;


    @BindString(R.string.my_courses_title)
    String mCoursesTitle;

    @BindDrawable(R.drawable.placeholder_icon)
    Drawable mUserPlaceholder;


    private List<FragmentBase> mFragments;
    private FragmentBase mCurrentFragment;
    private int mCurrentIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);
        ButterKnife.bind(this);

        View headerLayout = mNavigationView.getHeaderView(0);
        mProfileImage = ButterKnife.findById(headerLayout, R.id.profile_image);
        mUserNameTextView = ButterKnife.findById(headerLayout, R.id.username);


        setUpToolbar();
        setUpDrawerLayout();
        initFragments(savedInstanceState);

        bus.register(this);

        final SharedPreferenceHelper helper = mShell.getSharedPreferenceHelper();
        Profile cachedProfile = helper.getProfile();
        if (cachedProfile == null) { //todo always update??
            mShell.getApi().getUserProfile().enqueue(new Callback<StepicProfileResponse>() {
                @Override
                public void onResponse(Response<StepicProfileResponse> response, Retrofit retrofit) {
                    Profile profile = response.body().getProfile();

                    helper.storeProfile(profile);
                    bus.post(new ProfileCanBeShownEvent(profile));
                }

                @Override
                public void onFailure(Throwable t) {
                    // FIXME: 06.10.15 Sometimes profile is not load, investigate it! (maybe just set for update when create this activity)
                    mProfileImage.setVisibility(View.INVISIBLE);
                    mUserNameTextView.setText("");

                }
            });
        } else {
            bus.post(new ProfileCanBeShownEvent(cachedProfile));
        }
//        SharedPreferenceHelper sharedPreferenceHelper = mShell.getSharedPreferenceHelper();
//        AuthenticationStepicResponse resp = sharedPreferenceHelper.getAuthResponseFromStore(MainFeedActivity.this);

    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initFragments(Bundle savedInstance) {
        //// TODO: 22.12.15 optimize it, remove dependence on indices
        mFragments = new ArrayList<>();
        mFragments.add(new MyCoursesFragment());
//        mFragments.add(new BestLessons());
        mFragments.add(new FindCoursesFragment());
        mFragments.add(new DownloadsFragment());
        mFragments.add(new SettingsFragment());

        if (savedInstance == null) {
            mCurrentIndex = 0;
        } else {
            mCurrentIndex = savedInstance.getInt(KEY_CURRENT_INDEX);
        }

        showCurrentFragment();
    }

    private void showCurrentFragment() {
        Menu menu = mNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mCurrentIndex);
        menuItem.setChecked(true); //when we do not choose in menu
        showCurrentFragment(menuItem);
    }

    private void showCurrentFragment(MenuItem menuItem) {
        mCurrentFragment = mFragments.get(mCurrentIndex);
        setTitle(menuItem.getTitle());
        setFragment();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        //Check to see which item was being clicked and perform appropriate action
        switch (menuItem.getItemId()) {
            //todo: substitute to getting from provider
            case R.id.my_courses:
                mCurrentIndex = 0;
                break;
            case R.id.find_lessons:
                mCurrentIndex = 1;
                break;
            case R.id.my_settings:
                mCurrentIndex = 3;
                break;
            case R.id.cached_videos:
                mCurrentIndex = 2;
                break;
            case R.id.logout_item:
                YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_LOGOUT);

                LogoutAreYouSureDialog dialog = new LogoutAreYouSureDialog();
                dialog.show(getSupportFragmentManager(), null);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawers();
                    }
                }, 0);
                return true;

            default:
                Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                break;
        }
        showCurrentFragment(menuItem);
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
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_closed) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
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

    private void setFragment() {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, mCurrentFragment);
        fragmentTransaction.commit();
    }

    @Subscribe
    public void showProfile(ProfileCanBeShownEvent e) {
        Profile profile = e.getProfile();
        if (profile == null) {
            YandexMetrica.reportError(AppConstants.NULL_SHOW_PROFILE, new NullPointerException());
            return;
        }
        mProfileImage.setVisibility(View.VISIBLE);
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
        showCurrentFragment();
    }
}
