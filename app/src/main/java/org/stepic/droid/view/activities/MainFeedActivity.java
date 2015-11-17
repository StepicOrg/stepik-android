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

import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.model.Profile;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.dialogs.AreYouSureDialog;
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

    @Bind(R.id.profile_image)
    ImageView mProfileImage;

    @Bind(R.id.username)
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

        setUpToolbar();
        setUpDrawerLayout();
        initFragments();

        final SharedPreferenceHelper helper = mShell.getSharedPreferenceHelper();
        Profile cachedProfile = helper.getProfile();
        if (cachedProfile == null) { //todo always update??
            mShell.getApi().getUserProfile().enqueue(new Callback<StepicProfileResponse>() {
                @Override
                public void onResponse(Response<StepicProfileResponse> response, Retrofit retrofit) {
                    Profile profile = response.body().getProfile();

                    helper.storeProfile(profile);
                    showProfile(profile);
                }

                @Override
                public void onFailure(Throwable t) {
                    // FIXME: 06.10.15 Sometimes profile is not load, investigate it! (maybe just set for update when create this activity)
                    mProfileImage.setVisibility(View.INVISIBLE);
                    mUserNameTextView.setText("");

                }
            });
        } else {
            showProfile(cachedProfile);
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

    private void initFragments() {
        mFragments = new ArrayList<>();
        mFragments.add(new MyCoursesFragment());
//        mFragments.add(new BestLessons());
        mFragments.add(new FindCoursesFragment());
        mFragments.add(new SettingsFragment());

        mCurrentIndex = 0;
        showCurrentFragment();
    }

    private void showCurrentFragment() {
        mCurrentFragment = mFragments.get(mCurrentIndex);
        setFragment();
        Menu menu = mNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mCurrentIndex);
        menuItem.setChecked(false);
        setTitle(menuItem.getTitle());
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
                mCurrentIndex = 2;
                break;
            case R.id.logout_item:
                //todo: add 'Are you sure?" dialog
                YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_LOGOUT);

                AreYouSureDialog dialog = new AreYouSureDialog();
                dialog.show(getSupportFragmentManager(), null);

                menuItem.setChecked(false);
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

        mCurrentFragment = mFragments.get(mCurrentIndex);

        menuItem.setChecked(false);
        setTitle(menuItem.getTitle());
        setFragment();
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

    private void showProfile(Profile profile) {
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentIndex = savedInstanceState.getInt(KEY_CURRENT_INDEX);
        showCurrentFragment();
    }

    public void showFindLesson () {
        mCurrentIndex = 1;
        showCurrentFragment();
    }
}
