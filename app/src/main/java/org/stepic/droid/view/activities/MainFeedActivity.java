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
import org.stepic.droid.core.IShell;
import org.stepic.droid.view.fragments.AvailableCourses;
import org.stepic.droid.view.fragments.BestLessons;
import org.stepic.droid.view.fragments.FindLessons;
import org.stepic.droid.view.fragments.MyCourses;
import org.stepic.droid.view.fragments.MySettings;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainFeedActivity extends StepicBaseFragmentActivity {
    @Inject
    IShell mShell;


    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.navigation_view)
    NavigationView mNavigationView;

    @Bind(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle(R.string.my_courses_title);

//        SharedPreferenceHelper sharedPreferenceHelper = mShell.getSharedPreferenceHelper();
//        AuthenticationStepicResponse resp = sharedPreferenceHelper.getAuthResponseFromStore(MainFeedActivity.this);


        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Closing drawer on item click
                mDrawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //todo: substiture to getting from provider
                    case R.id.my_courses:
                        setFragment(new MyCourses());
                        return true;
                    case R.id.best_lessons:
                        setFragment(new BestLessons());
                        return true;
                    case R.id.available_courses:
                        setFragment(new AvailableCourses());
                        return true;
                    case R.id.find_lessons:
                        setFragment(new FindLessons());
                        return true;
                    case R.id.my_settings:
                        setFragment(new MySettings());
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
}
