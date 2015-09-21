package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.util.SharedPreferenceHelper;
import org.stepic.droid.view.fragments.AvailableCourses;
import org.stepic.droid.view.fragments.BestLessons;
import org.stepic.droid.view.fragments.FindCoursesFragment;
import org.stepic.droid.view.fragments.MyCoursesFragment;
import org.stepic.droid.view.fragments.MySettings;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class MainFeedActivity extends StepicBaseFragmentActivity {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.navigation_view)
    NavigationView mNavigationView;

    @Bind(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @BindString(R.string.my_courses_title)
    String mCoursesTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setMyCourses();

//        SharedPreferenceHelper sharedPreferenceHelper = mShell.getSharedPreferenceHelper();
//        AuthenticationStepicResponse resp = sharedPreferenceHelper.getAuthResponseFromStore(MainFeedActivity.this);


        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Closing drawer on item click
                mDrawerLayout.closeDrawers();
                menuItem.setChecked(false);

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //todo: substitute to getting from provider
                    case R.id.my_courses:
                        setMyCourses();
                        return true;
                    case R.id.best_lessons:
                        setTitle(R.string.best_lessons_title);
                        setFragment(new BestLessons());
                        return true;
                    case R.id.available_courses:
                        setTitle(R.string.available_courses_title);
                        setFragment(new AvailableCourses());
                        return true;
                    case R.id.find_lessons:
                        setTitle(R.string.find_courses_title);
                        setFragment(new FindCoursesFragment());
                        return true;
                    case R.id.my_settings:
                        setTitle(R.string.settings_title);
                        setFragment(new MySettings());
                        return true;
                    case R.id.logout_item:
                        //todo: add 'Are you sure?" dialog
                        SharedPreferenceHelper helper = mShell.getSharedPreferenceHelper();
                        helper.deleteAuthInfo(MainFeedActivity.this);
                        mShell.getScreenProvider().showLaunchScreen(MainFeedActivity.this, false);
                        return true;

                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });

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

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();


    }

    private void setFragment(StepicBaseFragment fragment) {
        Toast.makeText(getApplicationContext(), "we change fragment", Toast.LENGTH_SHORT).show();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    private void setMyCourses () {
        setTitle(mCoursesTitle);
        setFragment(new MyCoursesFragment());
    }
}
