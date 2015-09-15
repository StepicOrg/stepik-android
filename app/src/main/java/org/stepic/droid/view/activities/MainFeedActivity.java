package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.core.IShell;
import org.stepic.droid.util.SharedPreferenceHelper;
import org.stepic.droid.view.fragments.BestLessons;
import org.stepic.droid.view.fragments.MyCourses;
import org.stepic.droid.web.AuthenticationStepicResponse;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class MainFeedActivity extends StepicBaseFragmentActivity {
    @Inject
    IShell mShell;


    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.stepic.droid.R.layout.activity_main_feed);
        ButterKnife.bind(this);


        setTitle(org.stepic.droid.R.string.my_courses_title);
//        SharedPreferenceHelper sharedPreferenceHelper = mShell.getSharedPreferenceHelper();
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper();
        AuthenticationStepicResponse resp = sharedPreferenceHelper.getAuthResponseFromStore(MainFeedActivity.this);

        mToolbar = (Toolbar) findViewById(org.stepic.droid.R.id.toolbar);
        setSupportActionBar(mToolbar);

        //Initializing NavigationView
        mNavigationView = (NavigationView) findViewById(org.stepic.droid.R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {




                //Closing drawer on item click
                mDrawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {


                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case org.stepic.droid.R.id.first:
                        setFragment(new MyCourses());
                        return true;

                    // For rest of the options we just show a toast on click

                    case org.stepic.droid.R.id.second:
                        setFragment(new BestLessons());
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        mDrawerLayout = (DrawerLayout) findViewById(org.stepic.droid.R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, org.stepic.droid.R.string.drawer_open, org.stepic.droid.R.string.drawer_closed){

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


    private void setFragment (StepicBaseFragment fragment) {
        Toast.makeText(getApplicationContext(), "we change fragment", Toast.LENGTH_SHORT).show();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(org.stepic.droid.R.id.frame,fragment);
        fragmentTransaction.commit();
    }
//
//
//    @Override
//    public Map<Key<?>, Object> getScopedObjectMap() {
//        return null;
//    }
}
